FROM adoptopenjdk/openjdk8:alpine-slim
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources src/main/resources
ENTRYPOINT ["java","-jar","/app.jar"]