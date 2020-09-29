@quay.io/quarkus/ubi-quarkus-native-image
Feature: Verification of the encoding module

    Scenario: Check that encoding environment variables are defined
        Given container is started with entrypoint sh
        Then run sh -c 'env' in container and immediately check its output contains LANG=en_US.UTF-8
             and run sh -c 'env' in container and immediately check its output contains LANGUAGE=en_US:en
             and run sh -c 'env' in container and immediately check its output contains LC_ALL=en_US.UTF-8
