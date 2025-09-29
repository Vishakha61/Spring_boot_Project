FROM eclipse-temurin:23-jre-alpine

VOLUME /tmp

COPY target/billing-service-*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","/app.jar"]