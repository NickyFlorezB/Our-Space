import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

const PUBLIC_AUTH_ENDPOINTS = ['/auth/login', '/auth/refresh', '/auth/logout'];

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const isPublicAuthEndpoint = PUBLIC_AUTH_ENDPOINTS.some((endpoint) =>
    request.url.endsWith(endpoint),
  );

  if (isPublicAuthEndpoint) {
    return next(request);
  }

  const token = authService.getAccessToken();
  const authenticatedRequest = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || error.status !== 401 || !token) {
        return throwError(() => error);
      }

      return authService.refresh().pipe(
        switchMap(() => {
          const renewedToken = authService.getAccessToken();
          if (!renewedToken) {
            return throwError(() => error);
          }

          return next(request.clone({ setHeaders: { Authorization: `Bearer ${renewedToken}` } }));
        }),
        catchError((refreshError: unknown) => {
          void router.navigate(['/login']);
          return throwError(() => refreshError);
        }),
      );
    }),
  );
};
