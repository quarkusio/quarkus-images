IMAGE_VERSION := $(shell cat image.yaml | egrep ^version  | cut -d"\"" -f2)
BUILD_ENGINE := docker

.DEFAULT_GOAL := build

.PHONY: build
build:
    # workaround, use standalone docker-squash tool due a issue on squashing it with cekit3
    # to install it: https://github.com/goldmann/docker-squash
    # issue details: https://github.com/goldmann/docker-squash/issues/182
	cekit -v build --overrides-file quarkus-native-s2i-overrides.yaml $(BUILD_ENGINE) --no-squash
	docker-squash quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION} --tag=quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION}
	
	cekit -v build --overrides-file quarkus-maven-overrides.yaml $(BUILD_ENGINE) --no-squash
	docker-squash quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION} --tag=quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION}

	cekit -v build --overrides-file quarkus-native-image-overrides.yaml $(BUILD_ENGINE) --no-squash
	docker-squash quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION} --tag=quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION}

.PHONY: test
test: build
	cekit -v test --overrides-file quarkus-maven-overrides.yaml behave
	cekit -v test --overrides-file quarkus-native-image-overrides.yaml behave
	cekit -v test --overrides-file quarkus-native-s2i-overrides.yaml behave

.PHONY: push
push:
	docker push quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION}
	docker push quay.io/quarkus/ubi-quarkus-native-s2i:latest
	docker push quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION}
	docker push quay.io/quarkus/centos-quarkus-maven:latest
	docker push quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION}
	docker push quay.io/quarkus/ubi-quarkus-native-image:latest

.PHONY: clean
clean:
	rm -Rf target	