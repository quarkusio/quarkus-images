@quay.io/quarkus/ubi-quarkus-native-image
Feature: Verification of the GraalVM module

    Scenario: Check that native-image exists
        Given container is started with entrypoint sh
        Then run sh -c 'ls /opt/graalvm/bin/' in container and immediately check its output contains native-image

    Scenario: Check that native-image is in system path
        Given container is started with entrypoint sh
        Then run sh -c 'echo $PATH' in container and immediately check its output contains /opt/graalvm/bin

    Scenario: Check that JAVA_HOME is defined
        Given container is started with entrypoint sh
        Then run sh -c 'echo $JAVA_HOME' in container and immediately check its output contains /opt/graalvm

    Scenario: Check that java is GraalVM
        Given container is started with entrypoint sh
        Then run sh -c 'java --version' in container and immediately check its output contains GraalVM

    # TODO Can we test the entrypoint with --version?
    