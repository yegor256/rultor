#!/usr/bin/env bash

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
if [ "${squash}" == "true" ]; then
  args+=(--squash)
fi

BRANCH=__rultor
while [ "$(git show-branch "${BRANCH}" 2>/dev/null | wc -l)" -gt 0 ]; do
  BRANCH="__rultor-$(openssl rand -base64 32 | LC_CTYPE=C tr -dc 'a-zA-Z' | head -c 16)"
done
export BRANCH

git status
git checkout -B "${BRANCH}" "fork/${fork_branch}"
git checkout -B "${head_branch}" "origin/${head_branch}"

if [ "${rebase}" == "true" ]; then
  git checkout "${BRANCH}"
  git rebase "${head_branch}"
  git checkout "${head_branch}"
fi

# GPG key of Rultor, used to sign commits:
KEY=3FD3FA7E9AF0FA4C
git merge "--gpg-sign=${KEY}" --no-edit -m "${pull_title}" "${args[@]}" "${BRANCH}"
git log -1 --show-signature

docker_when_possible

git push origin "${head_branch}"
