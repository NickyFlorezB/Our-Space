FROM node:22-alpine AS build

WORKDIR /workspace
RUN corepack enable

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
RUN pnpm install --frozen-lockfile

COPY . .
RUN pnpm exec ng build --configuration production

FROM caddy:2-alpine

COPY --from=build /workspace/dist/frontend/browser /srv
