FROM ubuntu:14.04

MAINTAINER Dali Freire

ENV GIT_SSH_COMMAND ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no