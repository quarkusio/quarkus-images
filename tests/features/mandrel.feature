@quay.io/quarkus/ubi-quarkus-mandrel
Feature: Verification of the Mandrel module

    Scenario: Check that native-image exists
        Given container is started with entrypoint ls /opt/mandrel/bin/
        Then container log should contain native-image

    Scenario: Check that native-image is in system path
        Given container is started with entrypoint printenv PATH
        Then container log should contain /opt/mandrel/bin

    Scenario: Check that JAVA_HOME is defined
        Given container is started with entrypoint sh -c env
        Then container log should contain JAVA_HOME=/opt/mandrel

    Scenario: Check that native-image is Mandrel
        Given container is started with entrypoint native-image --version
        Then container log should contain Mandrel

    Scenario: Check that native-image still mentions GraalVM
        Given container is started with entrypoint native-image --version
        Then container log should contain GraalVM

    