@quay.io/quarkus/ubi-quarkus-native-image @quay.io/quarkus/ubi-quarkus-mandrel @quay.io/quarkus/centos-quarkus-maven
Feature: Verification of the workdir

    Scenario: Check that workdir is /project
        Given container is started with entrypoint pwd
        Then container log should contain /project
