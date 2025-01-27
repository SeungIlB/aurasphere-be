FROM eclipse-temurin:17-jdk-alpine

RUN apt-get update && apt-get install -y mysql-client

WORKDIR /app

# 정확한 .jar 파일을 지정
COPY build/libs/aurasphere-0.0.1-SNAPSHOT.jar /app/libs/app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "/app/libs/app.jar"]
