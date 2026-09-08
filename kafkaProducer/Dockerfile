FROM eclipse-temurin:17-jre
LABEL authors="pooriya"
WORKDIR /app
COPY kafkaProducer/target/kafkaProducer.jar app.jar
COPY crontab.txt crontab.txt
ENTRYPOINT ["java", "-jar", "app.jar"]