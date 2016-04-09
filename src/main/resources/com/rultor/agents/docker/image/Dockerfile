FROM ubuntu:14.04
MAINTAINER Armin Braun <me@obrown.io>
LABEL Description="This is the image holding the Rultor build runner infrastructure."

# UTF-8 locale
RUN locale-gen en_US en_US.UTF-8
RUN dpkg-reconfigure locales
ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

# Basics
RUN apt-get update && apt-get install -y bsdmainutils=9.0.5ubuntu1

# SSHD
RUN apt-get update && apt-get install -y ssh=1:6.6p1-2ubuntu2.6 && mkdir /var/run/sshd && \
    chmod 0755 /var/run/sshd
RUN mkdir /root/.ssh && ssh-keygen -f /root/.ssh/id_rsa -N ''
RUN cat /root/.ssh/id_rsa.pub >> /root/.ssh/authorized_keys && chmod 600 \
    /root/.ssh/authorized_keys
EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]
