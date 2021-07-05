FROM adoptopenjdk/openjdk16:alpine-slim
ARG JAR_FILE=discovery-service-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]