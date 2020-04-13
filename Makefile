IMAGE_VERSION := $(shell cat image.yaml | egrep ^version  | cut -d"\"" -f2)
BUILD_ENGINE := docker

.DEFAULT_GOAL := build

.PHONY: build
build:
	cekit  build --overrides-file quarkus-native-s2i-overrides-java8.yaml $(BUILD_ENGINE) --tag=quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION}-java8
	cekit  build --overrides-file quarkus-native-s2i-overrides-java11.yaml $(BUILD_ENGINE) --tag=quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION}-java11
	cekit  build --overrides-file quarkus-maven-overrides-java8.yaml $(BUILD_ENGINE) --tag=quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION}-java8
	cekit  build --overrides-file quarkus-maven-overrides-java11.yaml $(BUILD_ENGINE) --tag=quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION}-java11
	cekit  build --overrides-file quarkus-native-image-overrides-java8.yaml $(BUILD_ENGINE) --tag=quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION}-java8
	cekit  build --overrides-file quarkus-native-image-overrides-java11.yaml $(BUILD_ENGINE) --tag=quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION}-java11
	cekit  build --overrides-file quarkus-native-binary-s2i-overrides.yaml $(BUILD_ENGINE) --tag=quay.io/quarkus/ubi-quarkus-native-binary-s2i:${IMAGE_VERSION}

.PHONY: test
test: build
	cekit -v test --overrides-file quarkus-maven-overrides-java8.yaml behave
	cekit -v test --overrides-file quarkus-maven-overrides-java11.yaml behave
	cekit -v test --overrides-file quarkus-native-image-overrides-java8.yaml behave
	cekit -v test --overrides-file quarkus-native-image-overrides-java11.yaml behave
	cekit -v test --overrides-file quarkus-native-s2i-overrides-java8.yaml behave
	cekit -v test --overrides-file quarkus-native-s2i-overrides-java11.yaml behave
	cekit -v test --overrides-file quarkus-native-binary-s2i-overrides.yaml behave

.PHONY: push
push:
	docker push quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION}-java8
	docker push quay.io/quarkus/ubi-quarkus-native-s2i:${IMAGE_VERSION}-java11
	docker push quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION}-java8
	docker push quay.io/quarkus/centos-quarkus-maven:${IMAGE_VERSION}-java11
	docker push quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION}-java8
	docker push quay.io/quarkus/ubi-quarkus-native-image:${IMAGE_VERSION}-java11
	docker push quay.io/quarkus/ubi-quarkus-native-binary-s2i:${IMAGE_VERSION}

.PHONY: clean
clean:
	rm -Rf target	