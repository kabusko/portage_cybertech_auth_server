# portage_auth_server

A JWT Authorization Server built as a personal learning project to deepen hands-on understanding of JWT issuance, signing, key rotation, and JWKS publication. This is not a production system.

## What it does

- Issues signed RS256 access tokens on request.
- Publishes its current public signing keys via a JWKS endpoint, so a resource server can verify tokens without sharing any secret.
- Simulates key rotation on a fixed time interval, keeping the previous key available long enough for tokens signed with it to remain verifiable.

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5 (Web, Validation, Actuator) on embedded Tomcat |
| JWT / JWK | Nimbus JOSE + JWT (via `spring-security-oauth2-jose`) — used directly to sign tokens and build the JWKS |
| Boilerplate | Lombok |
| Build | Maven |
| Testing | JUnit 5, Mockito, AssertJ, Spring MockMvc |
| API contract | OpenAPI 3 ([`openapi.yaml`](./openapi.yaml)) |

## Architecture

The codebase follows a hexagonal (ports & adapters) layout: business logic lives in `core`, independent of any framework or transport; `infra` holds adapters that plug into it from either side.

`KeyStore` is the port the domain depends on; `InMemoryKeyStoreImpl` is the one adapter currently implementing it. Swapping storage strategies later (e.g. a different backing store) means adding a new adapter behind that same port — the domain code doesn't change.

## Key rotation & signing

- Signing algorithm: RS256, 2048-bit RSA keys.
- Rotation: every 5 minutes, on a wall-clock check (not tied to request volume).
- Access token TTL: 2 minutes.
- Key retention: the 2 most recent keys are kept published in JWKS, so a token issued just before rotation stays verifiable well past its own expiry.
- Storage: in-memory, intentionally — no keystore file or external secret store to configure, so the project runs with zero setup when paired with the resource server. Keys reset on restart; the resource server is expected to fetch JWKS live rather than caching/hardcoding a key.
- The first signing key is created on demand, when the first token is requested — not at server startup.

## Running locally

```
mvn spring-boot:run -Dspring-boot.run.profiles=localdev
```

Starts on port `8082` with actuator endpoints exposed for local inspection.

## Testing

```
mvn test
```

Current coverage: unit tests for the domain/service/persistence components (`AccessTokenGenerator`, `AccessTokenService`, `InMemoryKeyStoreImpl`) plus MockMvc slice tests for both REST controllers.

## Integration note (resource server)

This server's signing keys rotate and reset on restart. Any consuming resource server should fetch `/jwks.json` live (with normal HTTP caching, not a hardcoded key) rather than pinning a specific key at startup.

Because the first key is only created on demand, the JWKS endpoint is empty until the first access token has been issued. A resource server that fetches and caches JWKS before that happens may be stuck unable to verify tokens until its cache refreshes — requesting one token shortly after this server starts avoids the issue.
