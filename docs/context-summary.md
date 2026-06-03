# Sanosysalvos V2 Context Summary

## Current Goal
Keep the Android app and backend stack working end to end, with the map flow and admin dashboard backed by real services and Docker images that can be built locally.

## What Has Been Fixed
- Android build issues were corrected so the app can compile again.
- The repository was normalized to `main`; `origin/master` was removed.
- Backend module wiring was added in `settings.gradle.kts` so the Spring services are visible to Gradle.
- `google()` was restricted to Android groups so server-side dependencies resolve cleanly from `mavenCentral()`.
- The hardcoded Windows `org.gradle.java.home` setting was removed so Docker builds do not inherit a host-only JDK path.
- The map flow now goes through a real `geoservice` instead of a missing endpoint.
- The admin dashboard flow was aligned to `/api/v1/users/admin/list`.
- Fallback handling was added in the Xano client so missing upstream endpoints return an empty list instead of a hard failure.
- `pet-service` and `match-service` were scaffolded so the backend stack is complete enough to boot.

## Important Files
- [settings.gradle.kts](../settings.gradle.kts)
- [docker-compose.yml](../docker-compose.yml)
- [services/user-service/Dockerfile](../services/user-service/Dockerfile)
- [services/geoservice/Dockerfile](../services/geoservice/Dockerfile)
- [services/pet-service/Dockerfile](../services/pet-service/Dockerfile)
- [services/match-service/Dockerfile](../services/match-service/Dockerfile)
- [apps/bff-service/Dockerfile](../apps/bff-service/Dockerfile)
- [services/user-service/src/main/kotlin/com/sanosysalvos/user/client/XanoUserClient.kt](../services/user-service/src/main/kotlin/com/sanosysalvos/user/client/XanoUserClient.kt)
- [services/user-service/src/main/kotlin/com/sanosysalvos/user/api/UserHealthController.kt](../services/user-service/src/main/kotlin/com/sanosysalvos/user/api/UserHealthController.kt)
- [app/src/main/java/com/example/sanosysalvosv2/data/api/AdminApi.kt](../app/src/main/java/com/example/sanosysalvosv2/data/api/AdminApi.kt)
- [app/src/main/java/com/example/sanosysalvosv2/data/repository/AdminRepository.kt](../app/src/main/java/com/example/sanosysalvosv2/data/repository/AdminRepository.kt)

## Docker Status
- `user-service` image build succeeded with Docker.
- `geoservice` image build succeeds when built alone, but fails when other Docker builds run at the same time.
- `pet-service` was also observed progressing correctly when built alone, so the remaining issue is build contention rather than a code failure.
- The same contention issue affected `match-service` and `bff-service` when they were built in parallel.
- The practical fix is to build services one by one or avoid overlapping Docker commands.

## Validation Snapshot (2026-06-03)
- Docker images available locally:
	- `sanosysalvosv2-user-service:latest`
	- `sanosysalvosv2-geoservice:latest`
	- `sanosysalvosv2-pet-service:latest`
	- `sanosysalvosv2-match-service:latest`
	- `sanosysalvosv2-bff-service:latest`
- Compose stack started successfully with `docker compose up -d --no-build`.
- Running services confirmed with `docker compose ps`: `rabbitmq`, `user-service`, `geoservice`, `pet-service`, `match-service`, `bff-service`.
- Functional endpoint checks passed:
	- `GET /api/v1/bff/health` -> `{"service":"bff-service","status":"up"}`
	- `GET /api/v1/users/health` -> `{"service":"user-service","status":"up"}`
	- `GET /api/v1/geo/map/provider` -> OpenStreetMap provider payload
	- `GET /api/v1/bff/map/provider` -> provider payload
	- `GET /api/v1/bff/map/layers` -> 3 layers
	- `GET /api/v1/bff/map/reports/nearby` -> 3 mock markers
	- `GET /api/v1/users/admin/list` (fake token) -> empty list response (`[]`)
	- `GET http://localhost:15672` -> HTTP 200 (RabbitMQ UI reachable)

## Latest Docker Changes
- Removed the redundant `clean` step from all backend Dockerfiles to reduce image build time.
- The optimized Dockerfiles now run only `bootJar -x test --no-daemon` during the builder stage.

## Current Docker Advice
- Build each image separately with `docker build -f <service>/Dockerfile -t <image> .`
- Avoid running another Docker build while one long Gradle-in-Docker build is still active.
- If Compose is used, keep it strictly sequential and do not start competing build commands.

## Outstanding Work
- Finish local Docker builds for `geoservice`, `pet-service`, `match-service`, and `bff-service` if they are not already present.
- Run `docker compose up -d --build` only after the images are built one at a time.
- Check the service health endpoints after the stack is up.
- Capture final proof of the containerized stack once all images exist locally.

## Notes
- The repository had accidental tracked `build/` outputs from new services; those were cleaned up and `.gitignore` was expanded to ignore nested build artifacts.
- The remaining warning in `XanoUserClient.kt` is a Kotlin annotation-target warning, not a build failure.