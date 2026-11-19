FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml ./
COPY service/pom.xml service/pom.xml
COPY client/pom.xml client/pom.xml
COPY service/src service/src
RUN mvn -B -ntp -pl service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/service/target/service-1.0.0.jar /app/service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/service.jar"]
