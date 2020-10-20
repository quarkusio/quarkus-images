@quay.io/quarkus/ubi-quarkus-native-image @quay.io/quarkus/ubi-quarkus-mandrel @quay.io/quarkus/centos-quarkus-maven
Feature: Verification of the common packages module

    Scenario: Check that tar is installed
        Given container is started with entrypoint tar --version
        Then container log should contain GNU tar

    Scenario: Check that gzip is installed
        Given container is started with entrypoint gzip --version
        Then container log should contain gzip
    and container log should contain Copyright

    Scenario: Check that gcc is installed
        Given container is started with entrypoint gcc --version
        Then container log should contain GCC
    and container log should contain Copyright

    Scenario: Check that unzip is installed
        Given container is started with entrypoint unzip -h
        Then container log should contain extract
