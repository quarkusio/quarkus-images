@quay.io/quarkus/centos-quarkus-native-s2i
Feature: Perform s2i build

  Scenario: Verify if a Quarkus app is successfully built
    Given s2i build https://github.com/quarkusio/quarkus-quickstarts.git from getting-started
    Then file /home/quarkus/application should exist