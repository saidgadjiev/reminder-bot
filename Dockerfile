FROM openjdk:8-jre

WORKDIR /app
COPY ./target/app.jar .

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005", "app.jar"]