# Étape de construction
FROM maven:3.8.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY TourGuide . # Copie tout le contenu de 'TourGuide' dans le WORKDIR
RUN mvn install:install-file -Dfile=libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar && \
    mvn install:install-file -Dfile=libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar && \
    mvn install:install-file -Dfile=libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar && \
    mvn -f pom.xml clean package

# Étape d'exécution
FROM openjdk:17-jdk-slim
COPY --from=build /app/target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]