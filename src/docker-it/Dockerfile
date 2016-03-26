FROM ubuntu:14.04

RUN apt-get install -y ssh && mkdir /var/run/sshd && chmod 0755 /var/run/sshd
RUN mkdir /root/.ssh
COPY /id_rsa.pub /tmp
RUN cat /tmp/id_rsa.pub >> /root/.ssh/authorized_keys && chmod 600 /root/.ssh/authorized_keys
EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]