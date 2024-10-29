#!/bin/sh
# Copyright (c) 2009-2024 Yegor Bugayenko
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

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request, see http://doc.rultor.com/basics.html"
  exit -1
fi

if [[ "${tag}" =~ ^[a-zA-Z0-9\\.\\-]+$ ]]; then
  echo "tag name is valid: \"${tag}\""
else
  echo "tag name contains invalid characters: \"${tag}\""
  exit -1
fi

cd repo
if [ $(git tag -l "${tag}") ]; then
  echo "Tag ${tag} already exists!"
  exit -1
fi

export BRANCH_NAME=__rultor
while [ $(git show-branch "${BRANCH_NAME}" 2>/dev/null | wc -l) -gt 0 ]; do
  export BRANCH_NAME="__rultor-$(cat /dev/urandom | tr -cd 'a-z0-9' | head -c 16)"
done
git checkout -b "${BRANCH_NAME}"

docker_when_possible

for f in "${sensitive[@]}"; do
  if [ -e "${f}" ]; then
    echo "Sensitive file ${f} is present, can't release"
    exit -1
  fi
  echo "Sensitive file ${f} is absent, we are good"
done

git checkout "${BRANCH_NAME}"
git tag "${tag}" -m "${tag}: tagged by rultor.com"
git reset --hard
git clean -fd
git checkout "${head_branch}"
git branch -D "${BRANCH_NAME}"
git push --all origin
git push --tags origin
