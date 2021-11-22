@quay.io/quarkus/ubi-quarkus-native-image @quay.io/quarkus/ubi-quarkus-mandrel
Feature: Verification of the upx module

    Scenario: Check that upx is installed
        Given container is started with entrypoint upx --version
        Then container log should contain UCL

