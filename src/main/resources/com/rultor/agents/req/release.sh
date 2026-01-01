#!/usr/bin/env bash

# SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
# SPDX-License-Identifier: MIT

# shellcheck disable=SC2154

set -ex -o pipefail

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request, see http://doc.rultor.com/basics.html"
  exit 1
fi

if [[ "${tag}" =~ ^[a-zA-Z0-9\\.\\-]+$ ]]; then
  echo "tag name is valid: \"${tag}\""
else
  echo "tag name contains invalid characters: \"${tag}\""
  exit 1
fi

cd repo
if [ -n "$(git tag -l "${tag}")" ]; then
  echo "Tag ${tag} already exists!"
  exit 1
fi

BRANCH_NAME=__rultor
while [ "$(git show-branch "${BRANCH_NAME}" 2>/dev/null | wc -l)" -gt 0 ]; do
  BRANCH_NAME="__rultor-$(openssl rand -base64 32 | LC_CTYPE=C tr -dc 'a-zA-Z' | head -c 16)"
done
export BRANCH_NAME
git checkout -b "${BRANCH_NAME}"

docker_when_possible

for f in "${sensitive[@]}"; do
  if [ -e "${f}" ]; then
    echo "Sensitive file ${f} is present, can't release"
    exit 1
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
