FROM eclipse-temurin:23-jre-alpine

VOLUME /tmp

COPY target/inventory-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app.jar"]