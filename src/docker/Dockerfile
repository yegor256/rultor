# Copyright (c) 2009-2015, rultor.com
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

FROM ubuntu:14.04
MAINTAINER Yegor Bugayenko <yegor@teamed.io>
WORKDIR /tmp

ENV DEBIAN_FRONTEND=noninteractive

# UTF-8 locale
RUN locale-gen en_US en_US.UTF-8
RUN dpkg-reconfigure locales
ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

# Basic Linux tools
RUN apt-get update
RUN apt-get install -y wget bcrypt curl
RUN apt-get install -y unzip zip
RUN apt-get install -y gnupg gnupg2
RUN apt-get install -y jq
RUN apt-get install -y git git-core
RUN apt-get install -y bsdmainutils
RUN apt-get install -y nodejs
RUN apt-get install -y build-essential
RUN apt-get install -y automake autoconf

# SSH Daemon
RUN apt-get install -y ssh && \
  mkdir /var/run/sshd && \
  chmod 0755 /var/run/sshd

# Ruby
RUN apt-get install -y ruby ruby-dev
RUN gem update

# PHP
RUN apt-get install -y php5-dev php-pear
RUN mkdir jsl && \
  wget http://www.javascriptlint.com/download/jsl-0.3.0-src.tar.gz && \
  tar xzf jsl-0.3.0-src.tar.gz && \
  cd jsl-0.3.0/src && \
  make -f Makefile.ref && \
  mv Linux_All_DBG.OBJ/jsl /usr/local/bin && \
  cd .. && \
  rm -rf jsl
RUN pecl install xdebug && \
  echo "zend_extension=xdebug.so" > /etc/php5/cli/conf.d/xdebug.ini

# Java
RUN apt-get install -y default-jdk
RUN apt-get install -y gdebi && \
  wget http://ppa.launchpad.net/natecarlson/maven3/ubuntu/pool/main/m/maven3/maven3_3.2.1-0~ppa1_all.deb && \
  gdebi --non-interactive maven3_3.2.1-0~ppa1_all.deb && \
  ln -s /usr/share/maven3/bin/mvn /usr/bin/mvn && \
  rm -rf maven3_3.2.1-0~ppa1_all.deb
RUN git clone https://github.com/yegor256/rultor.git && \
  cd rultor && \
  gem install jekyll && \
  mvn clean install -Pqulice && \
  cd .. && \
  rm -rf rultor

