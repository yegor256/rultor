# Copyright (c) 2009-2017, rultor.com
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met: 1) Redistributions of source code must retain the above
# copyright notice, this list of conditions and the following
# disclaimer. 2) Redistributions in binary form must reproduce the above
# copyright notice, this list of conditions and the following
# disclaimer in the documentation and/or other materials provided
# with the distribution. 3) Neither the name of the rultor.com nor
# the names of its contributors may be used to endorse or promote
# products derived from this software without specific prior written
# permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
# NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
# FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
# THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
# STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
# OF THE POSSIBILITY OF SUCH DAMAGE.

FROM frolvlad/alpine-glibc
MAINTAINER Yegor Bugayenko <yegor@teamed.io>
LABEL Description="This is the default image for Rultor.com" Vendor="Rultor.com" Version="1.0"
WORKDIR /tmp

ENV LANG=en_US.UTF-8

# Basic Linux tools
RUN  echo "http://dl-2.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories; \
     echo "http://dl-3.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories; \
     echo "http://dl-4.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories; \
     echo "http://dl-5.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories; \
     echo "@testing http://nl.alpinelinux.org/alpine/edge/testing" >> /etc/apk/repositories; \
     echo "http://nl.alpinelinux.org/alpine/v3.3/main" >> /etc/apk/repositories
RUN apk update --no-cache && apk upgrade && \
apk add wget cryptsetup gcc g++ make curl ca-certificates unzip zip gnupg jq libxml2-utils libxml2-dev libxslt-dev postgresql-dev build-base automake autoconf openssh py-pip maven \
# Git 2.0
 py-setuptools git
# SSH Daemon
RUN mkdir /var/run/sshd && \
  chmod 0755 /var/run/sshd

# Ruby
RUN apk update --no-cache && apk --update add libatomic readline readline-dev libxml2  libxslt zlib-dev zlib yaml yaml-dev ncurses-terminfo-base ncurses-terminfo sudo bash libmagic zlib-dev gdbm libffi-dev fontconfig fontconfig-dev libstdc++ tzdata ruby-bigdecimal libgcrypt-dev ruby ruby-dev ruby-rake ruby-bundler libc-dev linux-headers openjdk7 ruby-nokogiri ruby-bundler perl findutils

# PHP
RUN sudo apk --update add php5 php5-dev php5-pear php5-curl php5-cli php5-json php5-phar php5-openssl php5-xml php5-xdebug php5-dom && rm /var/cache/apk/*
RUN mkdir /usr/local/bin/composer && curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin/composer --filename=composer

# S3cmd for AWS S3 integration
RUN pip install s3cmd

# NodeJS
RUN apk add --update nodejs
RUN npm install -g npm

# PhantomJS
RUN wget https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2 && \
tar xjf phantomjs-2.1.1-linux-x86_64.tar.bz2 && \
mv phantomjs-2.1.1-linux-x86_64/ /usr/local/bin/phantomjs/

#Javascriptlint
RUN sudo pip install jslint

#Latex
ENV PATH /usr/local/texlive/2016/bin/x86_64-linux:$PATH
RUN apk --no-cache add perl wget xz tar fontconfig-dev && \
    mkdir /tmp/install-tl-unx && \
    wget -qO- http://mirror.ctan.org/systems/texlive/tlnet/install-tl-unx.tar.gz | \
    tar -xz -C /tmp/install-tl-unx --strip-components=1 && \
    printf "%s\n" \
      "selected_scheme scheme-basic" \
      "option_doc 0" \
      "option_src 0" \
      > /tmp/install-tl-unx/texlive.profile && \
    /tmp/install-tl-unx/install-tl \
      --profile=/tmp/install-tl-unx/texlive.profile && \
    tlmgr install \
      babel babel-english collection-basic latex latex-bin latex-fonts latexconfig && \
    ( tlmgr install xetex || exit 0 ) && \
    rm -fr /tmp/install-tl-unx && \
    apk --no-cache del xz tar fontconfig-dev
RUN apk --no-cache add bash
RUN mkdir /workdir
WORKDIR /workdir
VOLUME ["/workdir"]
CMD ["bash"]
RUN tlmgr init-usertree

#JDK
RUN { \
		echo '#!/bin/sh'; \
		echo 'set -e'; \
		echo; \
		echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
	} > /usr/local/bin/docker-java-home \
	&& chmod +x /usr/local/bin/docker-java-home
ENV JAVA_HOME /usr/lib/jvm/java-1.7-openjdk
ENV PATH $PATH:/usr/lib/jvm/java-1.7-openjdk/jre/bin:/usr/lib/jvm/java-1.7-openjdk/bin

ENV JAVA_VERSION 7u121
ENV JAVA_ALPINE_VERSION 7.121.2.6.8-r0

RUN set -x \
	&& apk add --no-cache \
		openjdk7="$JAVA_ALPINE_VERSION" \
&& [ "$JAVA_HOME" = "$(docker-java-home)" ]

# Maven
ENV MAVEN_VERSION 3.3.9
ENV M2_HOME "/usr/local/apache-maven/apache-maven-${MAVEN_VERSION}"
RUN wget --quiet "http://mirror.dkd.de/apache/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
  mkdir -p /usr/local/apache-maven && \
  mv "apache-maven-${MAVEN_VERSION}-bin.tar.gz" /usr/local/apache-maven && \
  tar xzvf "/usr/local/apache-maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -C /usr/local/apache-maven/ && \
  ln -sfn /usr/bin/mvn mvn ${M2_HOME}/bin/mvn

RUN gem install jekyll:2.4.0 --no-document

ENV MAVEN_OPTS "-Xms512m -Xmx2g"
COPY settings.xml /root/.m2/settings.xml
RUN git clone https://github.com/yegor256/rultor.git && cd rultor && \
  mvn clean install -Pqulice && \
  cd .. && \
  rm -rf rultor

# Clean up
RUN rm -rf /tmp/*
RUN rm -rf /root/.ssh
