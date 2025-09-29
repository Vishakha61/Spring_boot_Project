FROM eclipse-temurin:23-jre-alpine

VOLUME /tmp

COPY target/product-service-*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","/app.jar"]