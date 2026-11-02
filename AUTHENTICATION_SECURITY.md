# Autenticación, cookies y seguridad

Esta guía describe el flujo de autenticación implementado en el frontend Angular y el backend Spring Boot. Explica las responsabilidades de cada bloque de código relacionado con login, comprobación de sesión, renovación y cierre de sesión; no pretende documentar línea por línea el resto de la aplicación.

## Resumen del flujo

| Dato | Dónde vive | Uso |
|---|---|---|
| Access token | En una signal de Angular, solo en memoria | Se envía como `Authorization: Bearer ...` a `/auth/me` y debe enviarse también en las demás llamadas que necesiten autenticación. |
| Refresh token | Cookie administrada por el navegador; nunca se lee desde Angular | Se envía automáticamente a `/auth/refresh` y `/auth/logout`; Spring la rota o revoca. |
| Hash del refresh token | Base de datos | Permite validar, rotar y revocar el token sin persistir su valor original. |

El refresh token **no se actualiza en una variable de memoria de JavaScript**. Al renovar, Spring devuelve un nuevo `Set-Cookie`; con `withCredentials: true`, el navegador reemplaza la cookie HttpOnly. Angular solo recibe y guarda el nuevo access token.

## Secuencia paso a paso

1. Angular envía `POST /api/v1/auth/login` con usuario y contraseña.
2. Spring valida el origen, autentica al usuario y crea un access token de 15 minutos y un refresh token de hasta 7 días.
3. La respuesta lleva el access token en JSON y el refresh token en `Set-Cookie`. El navegador guarda la cookie; JavaScript no puede leerla.
4. Angular conserva el access token en una signal y programa un temporizador cuyo primer disparo ocurre 10 minutos después del login.
5. Cada disparo llama `GET /api/v1/auth/me` con el access token en el encabezado Bearer.
6. Si `/me` responde correctamente, la sesión continúa y el temporizador espera otros 10 minutos.
7. Si `/me` falla, Angular llama `POST /api/v1/auth/refresh`. El navegador adjunta la cookie; Spring comprueba su hash, vigencia y revocación, revoca el token anterior y crea otro dentro de la misma familia.
8. Spring devuelve el access token nuevo en JSON y el refresh nuevo como cookie. Angular reemplaza el access token en memoria; el navegador reemplaza la cookie.
9. Si la renovación también falla, Angular borra el access token de memoria y detiene el temporizador.
10. Al cerrar sesión, Angular borra el access token y detiene el temporizador; el backend revoca el refresh token y expira la cookie.

El intervalo empieza **después de un login exitoso**, no al abrir la aplicación. Como el access token vive solo en memoria, recargar la página lo borra. En esta implementación no hay restauración automática de sesión al iniciar o recargar: el usuario deberá volver a iniciar sesión. Esto puede añadirse con un refresh inicial controlado por el arranque de Angular si se desea conservar la sesión entre recargas.

## Frontend: Angular

Archivo: [auth.service.ts](./frontend/src/app/core/auth/services/auth.service.ts)

### Imports y estado

- `DestroyRef`, `Injectable`, `inject` y `signal`: registran el servicio singleton, obtienen dependencias y mantienen el access token reactivo en memoria.
- `HttpClient` envía las peticiones; `HttpHeaders` construye el encabezado Bearer de `/me`.
- `Observable` describe peticiones HTTP, que Angular ejecuta cuando alguien se suscribe.
- `timer` crea el reloj RxJS; `exhaustMap` descarta un tick si el anterior todavía está en curso.
- `map` transforma la respuesta de `/me` o refresh a `void`; `tap` guarda efectos secundarios sin alterar la respuesta HTTP.
- `catchError` intercepta fallos para intentar el refresh y, si este falla, limpiar la sesión.
- `EMPTY` termina una comprobación sin emitir un valor ni dejar escapar un error al temporizador.
- `Subscription` permite detener el intervalo al cerrar sesión o destruir el servicio.
- `LoginRequest`, `LoginResponse` y `environment.apiUrl` tipan la petición/respuesta y definen el host de API.

### Constante y campos

- `SESSION_CHECK_INTERVAL_MS = 10 * 60 * 1000`: convierte diez minutos a milisegundos.
- `@Injectable({ providedIn: 'root' })`: comparte una instancia del servicio durante la vida de la aplicación.
- `http`: canal para llamadas REST.
- `destroyRef`: permite limpiar el intervalo cuando Angular destruye el servicio.
- `baseUrl`: prefijo configurado para la API; las rutas de autenticación añaden `/auth/...`.
- `accessToken`: signal que conserva únicamente el access token actual en memoria; no usa `localStorage` ni `sessionStorage`.
- `healthCheckSubscription`: referencia al intervalo para evitar duplicarlo y poder cancelarlo.
- El constructor registra `stopHealthCheck()` para limpiar el temporizador durante la destrucción del servicio.

### `login(credentials)`

- Hace `POST` a `/auth/login` con usuario y contraseña.
- `withCredentials: true` permite que el navegador acepte el `Set-Cookie` y envíe cookies en las peticiones cross-origin autorizadas por CORS.
- En `tap`, guarda `response.accessToken` en la signal y llama `startHealthCheck()` solo después de una respuesta exitosa.
- No extrae ni guarda el refresh token: la respuesta JSON ya no lo contiene.

### `refresh()`

- Hace `POST` a `/auth/refresh` con cuerpo vacío.
- `withCredentials: true` permite adjuntar la cookie HttpOnly aunque JavaScript no conozca su valor.
- En la respuesta, sustituye la signal con el access token nuevo.
- El `Set-Cookie` con el refresh rotado es procesado automáticamente por el navegador; Angular no lo inspecciona.

### `getAccessToken()`

- Devuelve el valor actual de la signal para que otros servicios puedan construir encabezados Bearer.
- Este método no persiste el token y no configura por sí mismo un interceptor HTTP global.

### `logout()`

- Limpia inmediatamente el access token local y cancela el intervalo.
- Retorna un `POST /auth/logout` con `withCredentials: true` para que el navegador envíe la cookie que el backend debe revocar y expirar.
- Como el observable HTTP es frío, el componente que llama a `logout()` debe suscribirse para que se envíe la petición al servidor.

### `startHealthCheck()`

- Comprueba si ya existe una suscripción activa; si existe, no crea otro temporizador.
- `timer(intervalo, intervalo)` espera diez minutos antes de la primera petición y luego emite cada diez minutos.
- `exhaustMap` mantiene una sola comprobación a la vez; ticks que coinciden con una llamada en curso no abren renovaciones paralelas desde esta instancia.
- Cada tick ejecuta `checkSession()`.

### `stopHealthCheck()`

- Cancela la suscripción si existe.
- Pone la referencia a `undefined` para permitir iniciar un nuevo intervalo tras otro login.

### `checkSession()`

- Lee el access token actual. Si no hay token, devuelve `EMPTY` y no hace una petición.
- Construye `Authorization: Bearer <access token>` para identificar la sesión sin usar la cookie de refresh.
- Llama `GET /auth/me`.
- Si `/me` tiene éxito, transforma el resultado a `void`; el temporizador sigue activo.
- Si `/me` falla por cualquier motivo, intenta `refresh()`. Esto también contempla errores temporales del servidor/red, no solo un access token expirado.
- Si refresh funciona, `refresh()` ya actualizó la signal y el navegador habrá actualizado la cookie a partir del `Set-Cookie`.
- Si refresh falla, limpia la signal, detiene el intervalo y consume el error con `EMPTY`. El servicio no muestra una alerta ni redirige por sí mismo.

Archivo: [auth.models.ts](./frontend/src/app/core/auth/models/auth.models.ts)

- `LoginRequest` define las dos propiedades enviadas al login.
- `LoginResponse` solo declara `accessToken`; omitir `refreshToken` del modelo hace explícito que Angular no recibe el refresh en JSON.

## Backend: Spring Boot

### `AuthController`

Archivo: [AuthController.java](./backend/src/main/java/com/nicky/ourspace/infrastructure/entrypoints/user/AuthController.java)

- `@RestController` registra los métodos como endpoints HTTP y serializa sus respuestas.
- `@RequestMapping("/auth")` agrega el prefijo `/auth`; junto con el context path `/api/v1`, produce `/api/v1/auth/...`.
- Las dependencias finales guardan los casos de uso/servicio y los valores de configuración de la cookie y del origen frontend.
- El constructor recibe `LoginUserUseCase`, `RefreshTokenService` y valores configurables mediante `@Value`; los valores predeterminados son para desarrollo local.
- `me(Authentication)` recibe la autenticación que creó el filtro JWT, extrae el UUID del principal y responde `{ "userId": ... }`. La configuración de seguridad exige autenticación para este endpoint.
- `login(request, credentials)` valida primero el encabezado `Origin`; luego obtiene usuario/contraseña del JSON y rechaza valores ausentes o en blanco. Ejecuta el caso de uso y entrega ambos tokens a `tokenResponse`.
- `refresh(request, refreshToken)` valida el origen, lee el refresh desde la cookie nombrada por configuración y devuelve `401` si falta. Llama al servicio de refresh y entrega el par rotado a `tokenResponse`.
- `logout(request, refreshToken)` valida origen; si hay cookie, revoca el token asociado. Después genera una cookie vacía con edad cero y los mismos atributos/path para que el navegador elimine la cookie existente; responde `204`.
- `tokenResponse(tokens)` crea `Set-Cookie`: `HttpOnly` impide leer el valor mediante `document.cookie`; `Secure` restringe el envío a HTTPS cuando se activa; `SameSite` controla envío cross-site; `Path` limita las rutas receptoras; `Max-Age` define una duración de siete días en el navegador. El cuerpo contiene solo `LoginResponseDto(accessToken)`.
- `validateOrigin(request)` compara el `Origin` exacto con `app.cors.allowed-origin`; ausencia o diferencia provoca `403`. Esto protege los endpoints POST que crean, rotan o revocan cookies frente a peticiones iniciadas desde otros orígenes.

### Seguridad HTTP

Archivo: [SecurityConfig.java](./backend/src/main/java/com/nicky/ourspace/application/configuration/SecurityConfig.java)

- `@Configuration` y `@EnableWebSecurity` habilitan la configuración de Spring Security.
- `securityFilterChain(...)` crea el filtro JWT y define las reglas HTTP.
- `.cors(...)` activa la configuración CORS del bean inferior.
- CSRF de Spring está deshabilitado porque el access token se envía explícitamente en un encabezado Bearer para acceder a recursos protegidos; las rutas de autenticación basadas en cookies se protegen adicionalmente mediante validación estricta de `Origin`.
- `STATELESS` y `NullSecurityContextRepository` evitan mantener sesión autenticada del lado servidor entre peticiones.
- Form login y HTTP Basic están desactivados; las credenciales se procesan con el endpoint propio.
- `/auth/login`, `/auth/refresh` y `/auth/logout` son públicos a nivel de filtro para que puedan iniciar o recuperar autenticación; cada operación de escritura comprueba origen en el controlador.
- `/auth/me` requiere un principal autenticado; el filtro establece ese principal solo si el JWT Bearer es válido.
- El registro de usuarios conserva su regla previa de acceso público por `POST /users`; `/me` conserva su regla previa.
- El filtro `JwtAuthenticationFilter` se añade a la cadena para validar el JWT antes de autorizar.
- `corsConfigurationSource(...)` permite solo el origen configurado, los métodos y encabezados declarados, y credenciales. CORS no es autenticación: controla qué orígenes del navegador pueden leer/responder a solicitudes cross-origin.
- `passwordEncoder()` usa BCrypt para validar contraseñas almacenadas.

### Filtro y JWT

Archivo: [JwtAuthenticationFilter.java](./backend/src/main/java/com/nicky/ourspace/infrastructure/drivenadapters/security/JwtAuthenticationFilter.java)

- Lee `Authorization`; si no empieza por `Bearer `, deja continuar la cadena sin establecer autenticación.
- Extrae el JWT que sigue al prefijo.
- `JwtService` valida firma y expiración y devuelve claims.
- Convierte el `sub` del JWT a UUID y construye una autenticación con ese usuario como principal.
- Guarda la autenticación en el `SecurityContext` de la petición actual.
- Si el token no se puede validar, responde `401` y corta la cadena; así `/auth/me` no llega al controlador con una autenticación nula.
- Si es válido, continúa la cadena.

Archivo: [JwtService.java](./backend/src/main/java/com/nicky/ourspace/infrastructure/drivenadapters/security/JwtService.java)

- Recibe la clave desde `security.jwt.secret` y crea una clave HMAC para JJWT.
- `generateAccessToken(userId)` coloca el UUID en el subject, registra emisión, fija expiración a 15 minutos y firma el JWT.
- `validateAndGetClaims(token)` verifica firma y claims; la biblioteca rechaza tokens inválidos o expirados.

### Login y almacenamiento del refresh

Archivos: [LoginUserUseCase.java](./backend/src/main/java/com/nicky/ourspace/application/usecases/authentication/LoginUserUseCase.java), [AuthenticationAdapter.java](./backend/src/main/java/com/nicky/ourspace/infrastructure/drivenadapters/adapters/AuthenticationAdapter.java), [AuthenticationTokensDto.java](./backend/src/main/java/com/nicky/ourspace/application/dto/AuthenticationTokensDto.java) y [LoginResponseDto.java](./backend/src/main/java/com/nicky/ourspace/application/dto/LoginResponseDto.java)

- `AuthenticationAdapter` busca usuario por nombre y compara contraseña con BCrypt; si coincide, genera el JWT y crea un refresh persistido.
- Si el usuario no existe o la contraseña no coincide, devuelve el mismo `401 Invalid username or password`; así no distingue entre esos dos casos al cliente.
- `LoginUserUseCase` delega la autenticación al puerto y convierte el mapa interno en `AuthenticationTokensDto`.
- `AuthenticationTokensDto` es un DTO interno que puede transportar los dos valores entre capas.
- `LoginResponseDto` es el DTO público: solo tiene `accessToken`; evita serializar el refresh en JSON.

Archivo: [RefreshTokenService.java](./backend/src/main/java/com/nicky/ourspace/infrastructure/drivenadapters/security/RefreshTokenService.java)

- `SecureRandom` genera 32 bytes impredecibles; Base64 URL-safe los convierte en el texto del refresh.
- `hashToken` aplica SHA-256 al token para buscarlo/persistirlo sin guardar su valor en claro.
- `create(userId)` registra hash, usuario, familia, fecha de creación y expiración inicial de siete días.
- `refresh(rawToken)` calcula el hash y busca el registro; token desconocido, revocado o expirado no puede renovar.
- Para una renovación válida marca revocado el token anterior, crea otro con el mismo usuario y familia, conserva la expiración original de la familia y genera un access token nuevo.
- `@Transactional` agrupa los cambios de rotación en una transacción.
- `revoke(rawToken)` busca por hash y fija la fecha de revocación si todavía estaba activo.

Archivos de persistencia: [RefreshTokenDbo.java](./backend/src/main/java/com/nicky/ourspace/infrastructure/drivenadapters/persistence/dbo/RefreshTokenDbo.java) y [JpaRefreshTokenRepository.java](./backend/src/main/java/com/nicky/ourspace/infrastructure/drivenadapters/persistence/repository/JpaRefreshTokenRepository.java)

- La entidad contiene UUID de usuario y familia, hash único, fechas de emisión/expiración y revocación.
- `findByTokenHash` permite validar el token que presentó el navegador.
- `findByFamilyId` permite consultar la familia de tokens rotados.

### Errores

Archivo: [GlobalExceptionHandler.java](./backend/src/main/java/com/nicky/ourspace/infrastructure/entrypoints/exception/GlobalExceptionHandler.java)

- Convierte excepciones de expiración/revocación de refresh a `401`.
- Un refresh desconocido también responde `401`, no revela si el token existe con un `404`.
- `ResponseStatusException` conserva el código HTTP elegido por el controlador, por ejemplo `403` por origen y `401` por cookie ausente.
- Las demás reglas de errores de la aplicación permanecen como estaban.

## Cookie HttpOnly y atributos

- **HttpOnly:** los scripts no pueden leer el valor con `document.cookie`. El navegador sí puede adjuntarlo a solicitudes HTTP elegibles. Reduce el robo directo de refresh tokens por XSS; no elimina XSS ni evita que un script inyectado realice peticiones como el usuario.
- **Secure:** evita enviar la cookie por HTTP. `REFRESH_COOKIE_SECURE` está en `false` por defecto para desarrollo HTTP local; en producción debe configurarse como `true` y el sitio debe operar por HTTPS.
- **SameSite:** el valor predeterminado es `Lax`. Las aplicaciones frontend/backend en sitios diferentes podrían requerir `SameSite=None; Secure`; en ese caso se debe mantener la comprobación de `Origin` y revisar cuidadosamente la estrategia anti-CSRF.
- **Path:** por defecto se limita a `/api/v1/auth`, reduciendo las rutas que reciben la cookie.
- **Max-Age:** la cookie del navegador se crea con siete días. El servidor es quien decide la validez: la rotación conserva la expiración original de la familia, por lo que renovar no extiende indefinidamente la sesión.
- **CORS con credenciales:** el navegador necesita que Angular use `withCredentials: true` y que Spring permita el origen exacto y credenciales. No se debe cambiar el origen permitido por `*` cuando hay credenciales.

## Configuración para despliegue

Configuración actual: [application.yaml](./backend/src/main/resources/application.yaml)

- `FRONTEND_ORIGIN` debe ser el origen público exacto del frontend, incluyendo esquema y puerto cuando aplique, y debe coincidir con `Origin` enviado por el navegador.
- `REFRESH_COOKIE_SECURE=true` es obligatorio en producción HTTPS.
- Mantener `SameSite=Lax` cuando frontend y API pertenecen al mismo sitio; usar `None` solo si el despliegue realmente es cross-site, siempre con Secure y revisión CSRF.
- La configuración existente también contiene credenciales de base de datos y una clave JWT fija para desarrollo. No deben reutilizarse en producción: deben trasladarse a variables/secret manager, y cualquier clave que se haya publicado debe rotarse.
- Usar HTTPS en frontend, API y conexiones internas sensibles; restringir CORS a los dominios reales.

## Límites y notas operativas

- La comprobación de diez minutos empieza al completar login. No se hace una llamada inmediatamente y no se restaura una sesión tras recargar la página.
- Cualquier fallo de `/me` inicia refresh (incluye fallos temporales de red/servidor). Si refresh falla, se borra el token de memoria, pero no se navega automáticamente a login ni se muestra un aviso.
- `exhaustMap` evita renovaciones simultáneas del temporizador en una única instancia del servicio; no coordina varias pestañas del navegador, que podrían intentar rotar la misma cookie a la vez.
- El temporizador no sustituye enviar el access token en las demás peticiones protegidas. El código actual no registra aquí un interceptor global.
- HttpOnly reduce exposición, pero una vulnerabilidad XSS aún podría ejecutar acciones desde el navegador. Mantener dependencias actualizadas, escapar contenido y aplicar una política CSP adecuada sigue siendo importante.
- Este documento cubre el flujo de autenticación tocado y sus controles directos; no es una auditoría de seguridad completa del resto de endpoints o infraestructura.
