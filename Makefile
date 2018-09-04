###################### PlayNet Makefile ######################
#
# Parts of this makefile are based upon github.com/kolide/kit
#

NAME         := votebot
REPO         := dev-talk
GIT_HOST     := github.com
REGISTRY     := quay.io
IMAGE        := dev-talk/votebot

VERSION = $(shell git describe --tags --always --dirty)

NAMESPACE	?= $(NAME)

DOCKER_CACHE ?= --no-cache
DOCKER_TAGS := -t $(REGISTRY)/$(IMAGE):$(VERSION) -t $(REGISTRY)/$(IMAGE):latest

-include .env

.PHONY: build
### MAIN STEPS ###

all: test install run
# build docker image

build:
	@docker build $(DOCKER_CACHE) --rm=true $(DOCKER_TAGS) -f Dockerfile .

# build the docker image
docker: build

# upload the docker image
upload:
	docker push $(REGISTRY)/$(IMAGE)

version:
	@echo $(VERSION)

# create build dir
.pre-build:
	@mkdir -p build

# helper to build new image and kick existing pod
update-deployment: docker upload clean restart-deployment

# delete existing pod to force imagePull (if latest)
restart-deployment:
	kubectl delete po -n $(NAMESPACE) -lapp=$(NAME)

# clean build results and delete all images
clean:
	docker rmi -f $(shell docker images -q --filter=reference=$(REGISTRY)/$(IMAGE)*)