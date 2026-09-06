FROM eclipse-temurin:17-jre
LABEL authors="pooriya"
WORKDIR /app/cron
COPY target/Pooriya-CronApp.jar /app/cron
COPY crontab.txt /app/cron
ENTRYPOINT ["java", "-jar"]
CMD ["Pooriya-CronApp.jar"]