# Copyright (c) 2009-2019, Yegor Bugayenko
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

# The software packages configured here (PHP, Node, Ruby, Java etc.) are for
# the convenience of the users going to use this default container.
# If you are going to use your own container, you may remove them.
# Rultor has no dependency on these packages.

FROM ubuntu:16.04
MAINTAINER Yegor Bugayenko <yegor256@gmail.com>
LABEL Description="This is the default image for Rultor.com" Vendor="Rultor.com" Version="1.0"
WORKDIR /tmp

ENV DEBIAN_FRONTEND=noninteractive

# UTF-8 locale
RUN apt-get clean && apt-get update && apt-get install -y locales && locale-gen en_US.UTF-8
ENV LC_ALL en_US.UTF-8
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US.UTF-8

# Basic Linux tools
RUN apt-get update && apt-get install -y wget bcrypt curl \
  sudo \
  unzip zip \
  gnupg gnupg2 \
  jq \
  netcat-openbsd \
  bsdmainutils \
  libxml2-utils \
  build-essential \
  automake autoconf

# Git 2.0
RUN apt-get install -y software-properties-common python-software-properties && \
  add-apt-repository ppa:git-core/ppa && \
  apt-get update && \
  apt-get install -y git git-core

# SSH Daemon
RUN apt-get install -y ssh && \
  mkdir /var/run/sshd && \
  chmod 0755 /var/run/sshd

# Ruby
RUN apt-get update && \
  apt-get install -y ruby-dev libmagic-dev zlib1g-dev && \
  gpg2 --keyserver hkp://keys.gnupg.net --recv-keys D39DC0E3 && \
  gpg2 --keyserver hkp://pool.sks-keyservers.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3 7D2BAF1CF37B13E2069D6956105BD0E739499BDB && \
  curl -L https://get.rvm.io | bash -s stable && \
  /bin/bash -l -c ". /etc/profile.d/rvm.sh && rvm install ruby-2.6.0 && rvm use 2.6.0"

# PHP
RUN LC_ALL=C.UTF-8 add-apt-repository ppa:ondrej/php && \
  apt-get update && \
  apt-get install -y php7.2 php-pear php7.2-curl php7.2-dev php7.2-gd php7.2-mbstring php7.2-zip php7.2-mysql php7.2-xml
RUN curl --silent --show-error https://getcomposer.org/installer | php && \
  mv composer.phar /usr/local/bin/composer
RUN mkdir jsl && \
  wget --quiet http://www.javascriptlint.com/download/jsl-0.3.0-src.tar.gz && \
  tar xzf jsl-0.3.0-src.tar.gz && \
  cd jsl-0.3.0/src && \
  make -f Makefile.ref && \
  mv Linux_All_DBG.OBJ/jsl /usr/local/bin && \
  cd .. && \
  rm -rf jsl
# RUN pecl install xdebug-beta && \
#   echo "zend_extension=xdebug.so" > /etc/php5/cli/conf.d/xdebug.ini

# Java
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886 E1DF1F24 3DD9F856
RUN apt-get update
RUN apt-get install -y openjdk-8-jdk ca-certificates maven
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64

# LaTeX
RUN apt-get install -y texlive-latex-base texlive-fonts-recommended texlive-latex-extra xzdec
RUN tlmgr init-usertree || echo 'Warning ignored'

# PhantomJS
RUN apt-get install -y phantomjs

# S3cmd for AWS S3 integration
RUN apt-get install -y s3cmd

# NodeJS
RUN rm -rf /usr/lib/node_modules && \
  curl -sL https://deb.nodesource.com/setup_6.x | bash - && \
  apt-get install -y nodejs

# Postgresql
RUN echo 'deb http://apt.postgresql.org/pub/repos/apt/ xenial-pgdg main' >> /etc/apt/sources.list.d/pgdg.list && \
  wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
RUN apt-get update -y && apt-get install -y postgresql-client-10 postgresql-10
USER postgres
RUN /etc/init.d/postgresql start && \
    psql --command "CREATE USER rultor WITH SUPERUSER PASSWORD 'rultor';" && \
    createdb -O rultor rultor
EXPOSE 5432
USER root
ENV PATH="${PATH}:/usr/lib/postgresql/10/bin"
# Postgresql service has to be started using `sudo /etc/init.d/postgresql start` in .rultor.yml

# Maven
ENV MAVEN_VERSION 3.3.9
ENV M2_HOME "/usr/local/apache-maven/apache-maven-${MAVEN_VERSION}"
RUN wget --quiet "http://mirror.dkd.de/apache/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" && \
  mkdir -p /usr/local/apache-maven && \
  mv "apache-maven-${MAVEN_VERSION}-bin.tar.gz" /usr/local/apache-maven && \
  tar xzvf "/usr/local/apache-maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -C /usr/local/apache-maven/ && \
  update-alternatives --install /usr/bin/mvn mvn "${M2_HOME}/bin/mvn" 1 && \
  update-alternatives --config mvn

# Warming it up a bit
RUN /bin/bash -l -c "gem install jekyll:3.4.3"
ENV MAVEN_OPTS "-Xms512m -Xmx2g"
COPY settings.xml /root/.m2/settings.xml
RUN git clone https://github.com/yegor256/rultor.git --depth=1
RUN cd rultor && \
  mvn clean install -DskipTests -Pqulice --quiet && \
  cd .. && \
  rm -rf rultor

# Clean up
RUN rm -rf /tmp/* && rm -rf /root/.ssh

ENTRYPOINT ["/bin/bash", "-l", "-c"]
