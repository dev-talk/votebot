FROM openjdk:10-jdk-slim

COPY build/libs/votebot-1.0-SNAPSHOT-withDependencies.jar votebot.jar

ENTRYPOINT ["java", "-jar", "-Xms256M", "-Xmx256M", "votebot.jar"]