FROM openjdk:11-jre

WORKDIR /app
COPY ./target/app.jar .

ENTRYPOINT ["java"]
CMD ["-Duser.timezone=UTC", "-Duser.language=ru", "-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "app.jar"]