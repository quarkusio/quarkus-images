# [Quarkus.io](http://quarkus.io) GraalVM Native S2I

## OpenShift

### Minishift 8 GB Set-Up recommendation

You need at least 8Gb of memory for minishift:

```
minishift profile delete quarkus-s2i-native
minishift profile set quarkus-s2i-native
minishift config set memory 8192
minishift start
```

### OpenShift Build & Use

This S2I Builder image is available on https://quay.io/repository/quarkus/ubi-quarkus-native-s2i.

The [https://quarkus.io/guides/kubernetes-guide](Quarkus documentation) explain how to use this s2i, including how to increase the `BuildConfig`'s `limits`.

## Locally (only for testing)

### Local Build

     cekit --verbose build --overrides-file centos-quarkus-native-s2i.yaml docker
