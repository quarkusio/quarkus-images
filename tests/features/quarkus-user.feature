@quay.io/quarkus/ubi-quarkus-native-image @quay.io/quarkus/ubi-quarkus-mandrel
Feature: Verification of the Quarkus user module

    Scenario: Check if quarkus user is correctly configured
        Given container is started with entrypoint id
        Then container log should contain id=1001(quarkus) gid=1001(quarkus) groups=1001(quarkus)

    Scenario: Check that the `Quarkus` group exists
        Given container is started with entrypoint cat /etc/group
        Then container log should contain quarkus:x:1001:

    Scenario: Check that the `Quarkus` user exists
        Given container is started with entrypoint cat /etc/passwd
        Then container log should contain quarkus:x:1001:1001:Quarkus user:/home/quarkus:/sbin/nologin

    Scenario: Check QUARKUS_HOME env variable
        Given container is started with entrypoint env
        Then container log should contain QUARKUS_HOME=/home/quarkus

    Scenario: Check that the container start with the Quarkus user
        Given container is started with command --version
        Then container log should contain GraalVM Version
