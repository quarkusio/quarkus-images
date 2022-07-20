# -- Stage ubi
FROM registry.access.redhat.com/ubi8/ubi-minimal:8.5 AS ubi
# -- Stage scratch
FROM registry.access.redhat.com/ubi8-micro:8.6 AS scratch
# -- Final Stage
FROM scratch
COPY --from=ubi /usr/lib64/libgcc_s.so.1 /usr/lib64/libgcc_s.so.1
COPY --from=ubi /usr/lib64/libstdc++.so.6 /usr/lib64/libstdc++.so.6
COPY --from=ubi /usr/lib64/libz.so.1 /usr/lib64/libz.so.1
