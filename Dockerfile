FROM gradle:4.10-alpine AS builder
WORKDIR /app
COPY . .
USER root
RUN ["gradle", "--no-daemon" ,"copyDependencies"]

FROM openjdk:8-jre-alpine
LABEL maintainer="docker@play-net.org"
WORKDIR /app
VOLUME /app/data/
EXPOSE 8080
COPY --from=builder app/build/dependencies dependencies
COPY --from=builder app/build/resources/main* /app/build/classes/java/main* classes/
CMD ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=2", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom", "-cp", "dependencies/*:classes", "net.dev_talk.votebot.core.Launcher"]
