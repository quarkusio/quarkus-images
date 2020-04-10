
.DEFAULT_GOAL := build

.PHONY: build
build:
	sh ./build-images.sh

.PHONY: test
test: build
	sh ./test-images.sh

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