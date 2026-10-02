# syntax=docker/dockerfile:1

########## BUILD STAGE ##########
FROM --platform=$BUILDPLATFORM gradle:8.13-jdk21 AS build
WORKDIR /workspace

# Gradle 메타만 먼저 복사해 의존성 레이어 캐시
COPY --chown=gradle:gradle gradle /workspace/gradle
COPY --chown=gradle:gradle gradlew settings.gradle build.gradle /workspace/

# 앱 소스 전체 복사 - 실제 빌드
COPY --chown=gradle:gradle . /workspace
RUN chmod +x gradlew && ./gradlew clean bootJar -x test --no-daemon

########## RUNTIME STAGE ##########
FROM eclipse-temurin:21-jre-noble
WORKDIR /app
RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --home-dir /app app
COPY --from=build /workspace/build/libs/app.jar /app/app.jar
RUN chown app:app /app/app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --start-period=60s --retries=6 \
  CMD curl --fail --silent http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
