@quay.io/quarkus/ubi-quarkus-native-image
Feature: Quarkus user features

    Scenario: Check if quarkus user is correctly configured
        Given container is started with command "sh"
        Then run sh -c 'id' in container and check its output for id=1001(quarkus) gid=1001(quarkus) groups=1001(quarkus)

    Scenario: Check that the `Quarkus` group exists
        Given container is started with command "sh"
        Then run sh -c 'cat /etc/group' in container and immediately check its output contains quarkus:x:1001:

    Scenario: Check that the `Quarkus` user exists
        Given container is started with command "sh"
        Then run sh -c 'cat /etc/passwd' in container and immediately check its output contains quarkus:x:1001:1001:Quarkus user:/home/quarkus:/sbin/nologin

    Scenario: Check QUARKUS_HOME env variable
        Given container is started with command "sh"
        Then run sh -c 'env' in container and immediately check its output contains QUARKUS_HOME=/home/quarkus
 