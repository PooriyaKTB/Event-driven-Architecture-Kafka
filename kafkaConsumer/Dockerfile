FROM eclipse-temurin:17-jre
LABEL authors="pooriya"
WORKDIR /app
COPY kafkaConsumer/target/kafkaConsumer.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]