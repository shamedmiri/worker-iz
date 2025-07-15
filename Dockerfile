 FROM openjdk:17-jdk-slim
WORKDIR /app
COPY myapp.jar .
CMD ["java", "-jar", "myapp.jar"]

