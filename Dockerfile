FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY app/build/distributions/app.tar .
RUN tar -xf app/build/distributions/app.tar -C /runtime --strip-components=1
RUN rm -rf /app

WORKDIR /runtime

CMD ["app/bin/application"]
