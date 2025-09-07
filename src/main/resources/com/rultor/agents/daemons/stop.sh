#!/usr/bin/env bash

# SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
# SPDX-License-Identifier: MIT

set -e

if [ ! -e cid ]; then
  echo "'cid' file is absent, Docker container is not running"
  exit
fi

cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  docker stop "${cid}"
  docker kill "${cid}"
  rm -f cid
fi
