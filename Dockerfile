FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY app/build/distributions/app.tar .
RUN tar -xf app.tar -C /runtime
RUN rm -rf /app

WORKDIR /runtime

CMD ["app/bin/app"]
