#!/bin/bash
docker build -t dev-talk/votebot .
docker login -u "$DOCKER_USERNAME" -p "$DOCKER_PASSWORD"
docker push dev-talk/votebot