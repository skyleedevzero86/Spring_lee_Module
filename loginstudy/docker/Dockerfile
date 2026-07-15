ARG MODULE=auth-server

FROM eclipse-temurin:21-jdk-alpine AS build
ARG MODULE
WORKDIR /workspace
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY auth-server ./auth-server
COPY member-service ./member-service
COPY user-portal ./user-portal
COPY admin-portal ./admin-portal
RUN chmod +x ./gradlew \
	&& ./gradlew :${MODULE}:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine AS runtime
ARG MODULE
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /workspace/${MODULE}/build/libs/*.jar /app/app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","/app/app.jar"]
