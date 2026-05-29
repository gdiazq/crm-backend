# Running the CRM backend with Docker

Each microservice has its own `Dockerfile` (multi-stage: Maven build → JRE 21 runtime)
and its own `docker-compose.yml`. They communicate over a **shared external network**
`crm-network`, and discover each other through Eureka (instances register by IP, so
`lb://mcsv-*` routing works across containers).

The database is **external (Supabase)** — there is no local Postgres container. DB
credentials, AWS keys and the JWT secret are injected from the root `.env` via `env_file`.

## One-time setup

```bash
docker network create crm-network
```

## Startup order

The services depend on each other, so bring them up in this order (each in its folder):

```bash
# 1. Discovery
(cd mcsv-eureka && docker compose up -d --build)

# 2. Config server
(cd mcsv-config && docker compose up -d --build)

# 3. Gateway (+ Redis) and the business services
(cd mcsv-gateway     && docker compose up -d --build)
(cd mcsv-auth        && docker compose up -d --build)
(cd mcsv-user        && docker compose up -d --build)
(cd mcsv-rrhh        && docker compose up -d --build)
(cd mcsv-project     && docker compose up -d --build)
(cd mcsv-recruitment && docker compose up -d --build)
```

| Service | Port | Notes |
|---|---|---|
| mcsv-eureka | 8761 | service discovery (start first) |
| mcsv-config | 8888 | config server (start second) |
| mcsv-gateway | 9090 | API gateway + Redis (6379) |
| mcsv-auth | 9092 | |
| mcsv-user | 9091 | |
| mcsv-rrhh | 9096 | |
| mcsv-project | 9097 | |
| mcsv-recruitment | 9098 | |

## Notes

- Services retry their connection to config/eureka on startup, so brief ordering gaps
  self-heal; `restart: unless-stopped` covers cold starts.
- Hostnames inside the network match the service names (`mcsv-config`, `mcsv-eureka`,
  `redis`), which is why each compose overrides `CONFIG_SERVER_URI`, `EUREKA_SERVER` and
  `REDIS_URL` to those names.
- To rebuild one service after a code change: `docker compose up -d --build` in its folder.
- Stop everything: run `docker compose down` in each folder (or
  `docker rm -f $(docker ps -aq --filter name=mcsv-)` plus `redis`).
