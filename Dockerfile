FROM eclipse-temurin:17

WORKDIR /opt/app

COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "/opt/app/app.jar"]