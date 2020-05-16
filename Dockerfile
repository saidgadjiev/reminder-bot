FROM openjdk:11-jre

WORKDIR /app
COPY ./target/app.jar .
COPY ./api.json .

ENTRYPOINT ["java"]
CMD ["-Duser.timezone=UTC", "-Duser.language=ru", "-jar", "app.jar"]
