# SPDX-FileCopyrightText: Copyright (c) 2024 Yegor Bugayenko
# SPDX-License-Identifier: MIT

FROM yegor256/rultor-ruby
LABEL Description="RULTOR image for simple Python+Pip projects" Version="0.0.0"

RUN apt-get update -y --fix-missing \
  && apt-get -y --no-install-recommends install software-properties-common=* \
  && apt-get clean

# Python3
RUN add-apt-repository -y ppa:deadsnakes/ppa \
  && apt-get update -y --fix-missing \
  && apt-get -y install libpq-dev libssl-dev openssl libffi-dev python3.7 python3-pip python3.7-dev \
  && rm -f /usr/lib/python3.*/EXTERNALLY-MANAGED \
  && ln -s $(which python3) /usr/bin/python \
  && bash -c 'python --version' \
  && pip3 install -Iv --upgrade pip \
  && bash -c 'pip --version'
