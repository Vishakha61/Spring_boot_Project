FROM eclipse-temurin:23-jre-alpine

VOLUME /tmp

COPY target/eureka-server-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","/app.jar"]