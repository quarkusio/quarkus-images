@quay.io/quarkus/centos-quarkus-native-s2i @quay.io/quarkus/centos-quarkus-maven @uay.io/quarkus/centos-quarkus-native-image
Feature: Quarkus common features

  Scenario: Check if quarkus user is correctly configured
    When container is started with command bash
    Then run sh -c 'id' in container and check its output for id=1001(quarkus) gid=1001(quarkus) groups=1001(quarkus)
