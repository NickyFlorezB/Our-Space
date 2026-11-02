# Docker deployment

The root `compose.yaml` runs PostgreSQL, the Spring API, and Caddy. PostgreSQL and
the API have no published host ports. Caddy serves the production Angular build,
proxies `/api/*` to the API, and binds only to `127.0.0.1:8080` for Tailscale Serve.

## Configure and start

```sh
cp .env.example .env
chmod 600 .env
```

Set `DB_PASSWORD` to the existing PostgreSQL role password when reusing the current
database volume. Set `JWT_SECRET` to a new random value of at least 32 bytes. Keep
`.env` out of source control. The storage host path and UID/GID must be writable by
the API container user.

For the existing database, retain `DB_VOLUME_NAME=our-space_pgdata` and the current
image storage directory. The old database has an empty `V1__init.sql` recorded in
Flyway history. Repair that old checksum once, then start the application:

```sh
docker compose --profile maintenance run --rm flyway repair
docker compose up -d --build --wait
```

Repair updates only Flyway's checksum metadata; it does not change application
tables or data. For an empty database, skip `repair`: API startup applies V1
automatically. Do not run `docker compose down -v` when preserving existing data.

The first request to the app should go through Tailscale Serve, forwarding HTTPS
to `http://127.0.0.1:8080`. Caddy handles HTTP inside Docker; Tailscale terminates
HTTPS. The API sets its refresh cookie as secure for that deployment.

The initial migration intentionally preserves the legacy `images.comment` column
found in the current database, alongside `comment_text` and `title`, so existing
photo metadata remains intact.
