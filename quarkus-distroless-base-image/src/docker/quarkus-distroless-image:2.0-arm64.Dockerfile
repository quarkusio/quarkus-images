# -- Stage debian
FROM debian:stable-slim AS debian
# -- Stage scratch
FROM gcr.io/distroless/cc AS scratch
# -- Final Stage
FROM scratch
COPY --from=debian /lib/aarch64-linux-gnu/libz.so.1 /lib/aarch64-linux-gnu/libz.so.1
