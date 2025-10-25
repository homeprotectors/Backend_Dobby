# syntax=docker/dockerfile:1

########## BUILD STAGE ##########
FROM gradle:8.13-jdk21 AS build
WORKDIR /workspace

# Gradle 메타만 먼저 복사해 의존성 레이어 캐시
COPY --chown=gradle:gradle gradle /workspace/gradle
COPY --chown=gradle:gradle gradlew settings.gradle build.gradle /workspace/

# gradlew 동작 확인(래퍼/버전 캐시)
RUN chmod +x gradlew && ./gradlew --version

# 의존성만 먼저 다운로드(변경 적으면 캐시 재사용)
RUN ./gradlew dependencies --no-daemon || true

# 앱 소스 전체 복사 - 실제 빌드
COPY --chown=gradle:gradle . /workspace
RUN ./gradlew clean bootJar -x test --no-daemon

########## RUNTIME STAGE ##########
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /workspace/build/libs/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
