# SPDX-FileCopyrightText: Copyright (c) 2024 Yegor Bugayenko
# SPDX-License-Identifier: MIT

FROM yegor256/rultor-ruby
LABEL Description="RULTOR image for simple LaTeX projects" Version="0.0.0"

RUN apt-get -y -q update \
  && apt-get -y install --no-install-recommends \
    wget=* \
    perl=* \
    zip=* unzip=* \
    inkscape=* \
    imagemagick=* \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

ENV TEXLIVE_YEAR=2024
ENV PATH=${PATH}:/usr/local/texlive/${TEXLIVE_YEAR}/bin/latest
# hadolint ignore=DL3003
RUN wget -q --no-check-certificate http://mirror.ctan.org/systems/texlive/tlnet/install-tl.zip \
  && unzip -qq install-tl.zip -d install-tl \
  && cd install-tl/install-tl-* \
  && echo "selected_scheme scheme-minimal" > p \
  && perl ./install-tl --profile=p \
  && ln -s "$(ls /usr/local/texlive/${TEXLIVE_YEAR}/bin/)" "/usr/local/texlive/${TEXLIVE_YEAR}/bin/latest" \
  && cd ~ && rm -rf install-tl* \
  && echo "export PATH=\${PATH}:/usr/local/texlive/${TEXLIVE_YEAR}/bin/latest" >> /root/.profile \
  && tlmgr init-usertree \
  && tlmgr install texliveonfly collection-latex l3kernel l3packages \
  && pdflatex --version \
  && bash -c '[[ "$(pdflatex --version)" =~ "2.6" ]]' \
  && tlmgr install latexmk \
  && bash -c 'latexmk --version' \
  && tlmgr install l3build \
  && bash -c 'l3build --version'

RUN tlmgr option repository ctan \
  && tlmgr --verify-repo=none update --self \
  && tlmgr --verify-repo=none install biber \
  && tlmgr --verify-repo=none install xetex

RUN apt-get -y -q update \
  && apt-get -y install --no-install-recommends \
    ghostscript=* \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

RUN bash -l -c ". /etc/profile.d/rvm.sh \
  && gem install texsc texqc \
  && texsc --version \
  && texqc --version"
