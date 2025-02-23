# SPDX-FileCopyrightText: Copyright (c) 2024 Yegor Bugayenko
# SPDX-License-Identifier: MIT

FROM yegor256/rultor-ruby
LABEL Description="RULTOR image for simple Java+Maven projects" Version="0.0.0"

ENV MAVEN_OPTS=-Xmx1g
ENV JAVA_OPTS=-Xmx1g
ENV JAVA_HOME=/usr/lib/jvm/java-17
RUN apt-get -y --fix-missing update \
  && apt-get -y --no-install-recommends install wget=* ca-certificates=* openjdk-17-jdk=* \
  && apt-get clean \
  && ln -s "/usr/lib/jvm/$(ls /usr/lib/jvm | grep java-1.17)" /usr/lib/jvm/java-17 \
  && echo 'export JAVA_HOME=/usr/lib/jvm/java-17' >> /root/.profile \
  && bash -c '[[ "$(javac  --version)" =~ "17.0" ]]'

ENV MAVEN_VERSION=3.9.6
ENV M2_HOME=/usr/local/apache-maven/apache-maven-${MAVEN_VERSION}
RUN echo 'export M2_HOME=/usr/local/apache-maven/apache-maven-${MAVEN_VERSION}' >> /root/.profile \
  && wget --quiet "https://dlcdn.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
  && mkdir -p /usr/local/apache-maven \
  && mv "apache-maven-${MAVEN_VERSION}-bin.tar.gz" /usr/local/apache-maven \
  && tar xzvf "/usr/local/apache-maven/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -C /usr/local/apache-maven/ \
  && update-alternatives --install /usr/bin/mvn mvn "${M2_HOME}/bin/mvn" 1 \
  && update-alternatives --config mvn \
  && mvn -version \
  && bash -c '[[ "$(mvn --version)" =~ "${MAVEN_VERSION}" ]]'
