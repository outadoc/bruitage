FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

COPY app/build/distributions/app.tar .
RUN mkdir /runtime && tar -xf app.tar -C /runtime
RUN rm -rf /app

WORKDIR /runtime

CMD ["app/bin/app"]
