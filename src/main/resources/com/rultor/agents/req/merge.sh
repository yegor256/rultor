#!/bin/bash

# SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
# SPDX-License-Identifier: MIT

# shellcheck disable=SC2154

set -ex -o pipefail

cd repo
git remote add fork "${fork}"
git remote update
args=()
if [ "${ff}" == "default" ]; then
  args+=(--ff)
fi
if [ "${ff}" == "no" ]; then
  args+=(--no-ff)
fi
if [ "${ff}" == "only" ]; then
  args+=(--ff-only)
fi

export BRANCH=__rultor
while [ "$(git show-branch "${BRANCH}" 2>/dev/null | wc -l)" -gt 0 ]; do
  BRANCH="__rultor-$(openssl rand -base64 32 | LC_CTYPE=C tr -dc 'a-zA-Z' | head -c 16)"
  export BRANCH
done

git status
git checkout -B "${BRANCH}" "fork/${fork_branch}"
git checkout -B "${head_branch}" "origin/${head_branch}"

if [ "${rebase}" == "true" ]; then
  git checkout "${BRANCH}"
  git rebase "${head_branch}"
  git checkout "${head_branch}"
fi

before=$(git config --global user.signingkey)
git config --global user.signingkey 3FD3FA7E9AF0FA4C
if [ "${squash}" == "true" ]; then
  git merge "${args[@]}" --squash "${BRANCH}"
  git commit -m "${pull_title}"
else
  git merge "${args[@]}" "${BRANCH}"
fi
if [ -n "${before}" ]; then
  git config --global user.signingkey "${before}"
fi

docker_when_possible

git push origin "${head_branch}"
