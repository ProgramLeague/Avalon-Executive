FROM busybox:latest
MAINTAINER Ray Eldath <eldath.ray@protonmail.com>
RUN mkdir /sandbox
RUN adduser sandbox -u 1111 -h /sandbox -D
COPY sandbox /sandbox
CMD while true; do sleep 1; done