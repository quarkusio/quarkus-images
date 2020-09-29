@quay.io/quarkus/ubi-quarkus-native-image
Feature: Verification of the common packages module

    Scenario: Check that tar is installed
        Given container is started with entrypoint sh
        Then run sh -c 'tar --version' in container once

    Scenario: Check that gzip is installed
        Given container is started with entrypoint sh
        Then run sh -c 'gzip --version' in container once        

    Scenario: Check that gcc is installed
        Given container is started with entrypoint sh
        Then run sh -c 'gcc --version' in container once              

    Scenario: Check that unzip is installed
        Given container is started with entrypoint sh
        Then run sh -c 'unzip -h' in container once
