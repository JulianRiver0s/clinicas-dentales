# Clínicas Dentales

API de citas (`citas-api`) + servicio de notificaciones (`notification-service`) + PostgreSQL, orquestados con Docker Compose.

Los JWT se firman con **RS256** (par de llaves RSA). La llave privada **no está en el repo**: cada quien la genera una vez (paso obligatorio antes de correr).

## Requisitos

- Java 17 (Maven va incluido vía el wrapper `./mvnw`)
- Docker + Docker Compose
- `ssh-keygen` y `openssl` (ambos vienen con Git Bash en Windows)

## 1. Generar llaves JWT (una sola vez)

Desde la raíz del repo, en Git Bash:

```bash
cd citas-api/src/main/resources/certs
ssh-keygen -t rsa -b 2048 -m PEM -f rsa_tmp -N "" -C ""    # genera RSA-2048 (PKCS#1)
openssl pkcs8 -topk8 -nocrypt -in rsa_tmp -out private.pem  # privada -> PKCS#8 (BEGIN PRIVATE KEY)
openssl rsa -in rsa_tmp -pubout -out public.pem            # pública -> SPKI  (BEGIN PUBLIC KEY)
rm -f rsa_tmp rsa_tmp.pub
```

Resultado: `certs/private.pem` y `certs/public.pem`. Ambos están en `.gitignore`.

> Sin `openssl` puedes usar solo `ssh-keygen` (requiere OpenSSH moderno):
> ```bash
> ssh-keygen -t rsa -b 2048 -f private.pem -N "" -C ""
> ssh-keygen -p -P "" -N "" -m PKCS8 -f private.pem    # privada -> PKCS#8
> ssh-keygen -e -m PKCS8 -f private.pem > public.pem   # pública -> SPKI
> rm -f private.pem.pub
> ```

## 2. Correr

**Con Docker (todo el stack):**
```bash
docker compose up --build
```
Las llaves generadas en el paso 1 se empaquetan en la imagen al hacer build.

**Solo `citas-api` en local** (necesita Postgres; lo más fácil es `docker compose up postgres`):
```bash
cd citas-api
./mvnw spring-boot:run
```

## 3. Probar

```bash
curl -s -X POST http://localhost:8080/token \
  -d "grantType=password" \
  -d "username=admin@mail.com" \
  -d "password=admin"
```
Devuelve `{"accessToken":"<jwt>"}` cuyo header es `{"alg":"RS256"}`. Úsalo como `Authorization: Bearer <accessToken>` en los endpoints protegidos.

> Para recibir también un refresh token: añade `-d "withRefreshToken=true"`.

## Producción

No commitees `private.pem`. Móntala como secreto y apunta las rutas por variable de entorno:

```
JWT_PRIVATE_KEY=file:/run/secrets/private.pem
JWT_PUBLIC_KEY=file:/run/secrets/public.pem
```
(Por defecto son `classpath:certs/private.pem` y `classpath:certs/public.pem`.)
