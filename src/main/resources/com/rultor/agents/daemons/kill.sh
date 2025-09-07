#!/usr/bin/env bash

# SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
# SPDX-License-Identifier: MIT

set -e -o pipefail

if [ -e cid ]; then
  cid=$(cat cid)
  if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
    docker rm -f "${cid}"
  fi
  rm -f cid
fi

if [ ! -e pid ]; then
  exit
fi
pid=$(cat pid)

if [ -n "$(ps -p "${pid}" -opid=)" ]; then
  kill "${pid}"
fi

sleep 15
if [ -n "$(ps -p "${pid}" -opid=)" ]; then
  kill -9 "${pid}"
fi
rm -f pid
