FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /workspace

COPY gradlew ./gradlew
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies

COPY src ./src

RUN ./gradlew --no-daemon bootJar
RUN set -eu; \
    jar_file=$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' -print -quit); \
    test -n "$jar_file"; \
    cp "$jar_file" /workspace/store-app.jar

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --home-dir /app --no-create-home app

COPY --from=build --chown=app:app /workspace/store-app.jar /app/store-app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/store-app.jar"]
