FROM eclipse-temurin:21.0.10_7-jdk-jammy

WORKDIR /app

COPY app/build/distributions/app.tar .
RUN mkdir /runtime && tar -xf app.tar -C /runtime
RUN rm -rf /app

WORKDIR /runtime

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD pgrep -x java || exit 1

CMD ["app/bin/app"]
