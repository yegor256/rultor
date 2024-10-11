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

RUN gem install texsc texqc
