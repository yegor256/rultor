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

cd repo
git remote add fork "${fork}"
git remote update
args=""
if [ "${ff}" == "default" ]; then
  args="${args} --ff"
fi
if [ "${ff}" == "no" ]; then
  args="${args} --no-ff"
fi
if [ "${ff}" == "only" ]; then
  args="${args} --ff-only"
fi

export BRANCH=__rultor
while [ $(git show-branch "${BRANCH}" 2>/dev/null | wc -l) -gt 0 ]; do
  export BRANCH="__rultor-$(cat /dev/urandom | tr -cd 'a-z0-9' | head -c 16)"
done

git status
git checkout -B "${BRANCH}" "fork/${fork_branch}"
git checkout -B "${head_branch}" "origin/${head_branch}"

if [ "${rebase}" == "true" ]; then
  git checkout "${BRANCH}"
  git rebase "${head_branch}"
  git checkout "${head_branch}"
fi

if [ "${squash}" == "true" ]; then
  git merge ${args} --squash "${BRANCH}"
  git commit -m "${pull_title}"
else
  git merge ${args} "${BRANCH}"
fi

docker_when_possible

git push origin "${head_branch}"
