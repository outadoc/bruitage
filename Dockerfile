FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Gradle wrapper + config
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
RUN chmod +x gradlew

# Sources
COPY src src

# Build distribution
RUN ./gradlew --no-daemon app:distribution:assembleDist \
    && mkdir /runtime \
    && tar -xf app/build/distributions/*.tar -C /runtime --strip-components=1 \
    && rm -rf /app/*

WORKDIR /runtime

CMD ["bin/application"]
