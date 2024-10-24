# Copyright (c) 2024 Yegor Bugayenko
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
