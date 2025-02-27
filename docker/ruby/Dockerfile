# SPDX-FileCopyrightText: Copyright (c) 2024 Yegor Bugayenko
# SPDX-License-Identifier: MIT

FROM ruby:3.2-bullseye
LABEL Description="RULTOR image for simple Ruby projects (gems mostly)" Version="0.0.0"
WORKDIR /tmp

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y locales \
  && sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen \
  && locale-gen \
  && update-locale LANG=en_US.UTF-8 \
  && echo "LC_ALL=en_US.UTF-8\nLANG=en_US.UTF-8\nLANGUAGE=en_US.UTF-8" > /etc/default/locale \
  && echo 'export LC_ALL=en_US.UTF-8' >> /root/.profile \
  && echo 'export LANG=en_US.UTF-8' >> /root/.profile \
  && echo 'export LANGUAGE=en_US.UTF-8' >> /root/.profile

ENV LC_ALL=en_US.UTF-8
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US.UTF-8

RUN apt-get update \
  && apt-get install -y --no-install-recommends \
    curl \
    sudo \
    git \
    unzip \
    zip \
    gnupg2 \
    ssh \
    libcurl4-gnutls-dev \
    libxml2-utils \
    libjpeg-dev \
    build-essential \
    libmagic-dev \
    zlib1g-dev \
    openssl \
  && apt-get clean

RUN mkdir -p /tmp/download \
  && curl -s -L "https://download.docker.com/linux/static/stable/x86_64/docker-27.3.1.tgz" | tar -xz -C /tmp/download \
  && mv /tmp/download/docker/docker /usr/bin/ \
  && rm -rf /tmp/download

RUN echo 'gem: --no-document' >> ~/.gemrc \
  && gem install bundler -v 2.3.26 \
  && gem install xcop -v 0.8.0 \
  && gem install pdd -v 0.23.1

ENTRYPOINT ["/bin/bash", "--login", "-c"]
