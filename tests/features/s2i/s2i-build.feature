@quay.io/quarkus/ubi-quarkus-native-s2i
Feature: Perform s2i build

  Scenario: Verify if a Quarkus app is successfully built (Maven)
    Given s2i build https://github.com/quarkusio/quarkus-quickstarts.git from getting-started
    Then file /home/quarkus/application should exist      

  Scenario: Verify if a Quarkus app is successfully built (Gradle)
    Given s2i build https://github.com/cescoffier/quarkus-quickstart-gradle.git
    Then file /home/quarkus/application should exist