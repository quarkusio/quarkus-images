@quay.io/quarkus/ubi-quarkus-native-image @quay.io/quarkus/ubi-quarkus-mandrel @quay.io/quarkus/centos-quarkus-maven
Feature: Verification of the encoding module

    Scenario: Check that encoding environment variables are defined
        Given container is started with entrypoint sh -c 'env'
        Then container log should contain LANG=en_US.UTF-8
             and container log should contain LANGUAGE=en_US:en
             and container log should contain LC_ALL=en_US.UTF-8
