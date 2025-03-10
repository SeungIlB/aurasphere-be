FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# .env 파일을 복사
COPY .env .env

# 정확한 .jar 파일을 지정
COPY build/libs/aurasphere-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}", "app.jar"]