@quay.io/quarkus/ubi-quarkus-native-image
Feature: Verification of the GraalVM module

    Scenario: Check that native-image exists
        Given container is started with entrypoint ls /opt/graalvm/bin/
        Then container log should contain native-image

    Scenario: Check that native-image is in system path
        Given container is started with entrypoint printenv PATH
        Then container log should contain /opt/graalvm/bin

    Scenario: Check that JAVA_HOME is defined
        Given container is started with entrypoint sh -c env
        Then container log should contain JAVA_HOME=/opt/graalvm

    Scenario: Check that java is GraalVM
        Given container is started with entrypoint java --version
        Then container log should contain GraalVM

    Scenario: Check that native-image is GraalVM
        Given container is started with entrypoint native-image --version
        Then container log should contain GraalVM

    Scenario: Check that native-image is not Mandrel
        Given container is started with entrypoint native-image --version
        Then container log should not contain Mandrel

    