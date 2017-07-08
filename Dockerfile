FROM busybox:latest
MAINTAINER Ray Eldath <eldath.ray@protonmail.com>
RUN mkdir /usr/sbin/sandbox
RUN adduser sandbox -u 1111 -h /sandbox -D
COPY sandbox /usr/sbin/
CMD while true; do sleep 1; done