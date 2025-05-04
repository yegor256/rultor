#!/bin/bash

# SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
# SPDX-License-Identifier: MIT

set -e -o pipefail

if [ ! -e pid ]; then
  echo "'pid' file is absent on the server after the end of operation; it seems that we didn't manage to start Docker container correctly"
  exit 1
fi

pid=$(cat pid)
if ps -p "${pid}" >/dev/null; then
  exit
fi

if [ ! -e cid ]; then
  echo "'cid' file is absent, most probably the Docker container wasn't started correctly"
  exit 1
fi

cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  echo "container ${cid} is alive"
else
  echo "container ${cid} is dead"
  date
  exit 1
fi
