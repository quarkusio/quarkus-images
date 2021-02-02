@quay.io/quarkus/ubi-quarkus-native-image @quay.io/quarkus/centos-quarkus-maven
Feature: Verification of the GraalVM module

    Scenario: Check that native-image exists
        Given container is started with entrypoint ls /opt/graalvm/bin/
        Then container log should contain native-image

    Scenario: Check that native-image is in system path
        Given container is started with entrypoint printenv PATH
        Then container log should contain /opt/graalvm/bin

    Scenario: Check that JAVA_HOME is defined
        Given container is started with entrypoint sh -c env
        Then container log should contain JAVA_HOME=/opt/graalvm

    Scenario: Check that java is GraalVM
        Given container is started with entrypoint java --version
        Then container log should contain GraalVM

    Scenario: Check that native-image is GraalVM
        Given container is started with entrypoint native-image --version
        Then container log should contain GraalVM

    Scenario: Check that native-image is not Mandrel
        Given container is started with entrypoint native-image --version
        Then container log should not contain Mandrel

    Scenario: Check that GraalVM native-image compiles HelloWorld
        Given container is started with entrypoint sh -c "echo 'public class Test {public static void main(String[] args) {System.out.println(\"Hello World!\");}}' > Test.java && javac Test.java && native-image Test && ./test" in container
        Then container log should contain Hello World!
