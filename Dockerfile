FROM openjdk:11-jdk as builder

ADD ./ /mnt
WORKDIR /mnt

RUN chmod +x ./gradlew
RUN ./gradlew

CMD ["/bin/bash"]

FROM openjdk:10-jdk-slim

LABEL maintainer PlayNet <docker@play-net.org>
LABEL type "public"

COPY --from=builder /mnt/build/libs/votebot-1.0-SNAPSHOT-withDependencies.jar votebot.jar

ENTRYPOINT ["java", "-jar", "-Xms256M", "-Xmx256M", "votebot.jar"]
