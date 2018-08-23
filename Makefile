###################### PlayNet Makefile ######################
#
# Parts of this makefile are based upon github.com/kolide/kit
#

NAME         := votebot
REPO         := dev-talk
GIT_HOST     := github.com
REGISTRY     := quay.io
IMAGE        := dev-talk/votebot

PATH := $(GOPATH)/bin:$(PATH)
TOOLS_DIR := cmd
VERSION = $(shell git describe --tags --always --dirty)
BRANCH = $(shell git rev-parse --abbrev-ref HEAD)
REVISION = $(shell git rev-parse HEAD)
REVSHORT = $(shell git rev-parse --short HEAD)
USER = $(shell whoami)

NAMESPACE	?= $(NAME)
DOCKER_CACHE ?= --no-cache
DOCKER_TAGS := -t $(REGISTRY)/$(IMAGE):$(VERSION) -t $(REGISTRY)/$(IMAGE):latest

-include .env

.PHONY: build

### MAIN STEPS ###

all: test install run

# install required tools and dependencies
deps:
	go get -u github.com/golang/dep/cmd/dep
	go get -u github.com/golang/lint/golint
	go get -u github.com/haya14busa/goverage
	go get -u github.com/kisielk/errcheck
	go get -u github.com/maxbrunsfeld/counterfeiter
	go get -u github.com/onsi/ginkgo/ginkgo
	go get -u github.com/onsi/gomega
	go get -u github.com/schrej/godacov
	go get -u golang.org/x/tools/cmd/goimports

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
