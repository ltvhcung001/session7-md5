FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn install -N -Dmaven.test.skip=true
COPY common-library ./common-library
RUN mvn clean install -f common-library/pom.xml -Dmaven.test.skip=true
COPY payment-service ./payment-service
RUN mvn clean package -f payment-service/pom.xml -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/payment-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
