FROM openjdk:11-jre-slim as builder
WORKDIR /app
COPY . /app
RUN ["/bin/sh", "gradlew", "--no-daemon" ,"copyDependencies"]

FROM openjdk:11-jre-slim
LABEL maintainer="docker@play-net.org"
WORKDIR /app
VOLUME /app/logs/
COPY --from=builder app/build/dependencies dependencies
COPY --from=builder app/build/resources/main* /app/build/classes/java/main* classes/
CMD ["java", "-XX:+ExitOnOutOfMemoryError", "-Djava.security.egd=file:/dev/./urandom" ,"-cp", "dependencies/*:classes", "net.dev_talk.votebot.core.Launcher"]
