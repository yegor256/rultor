#!/usr/bin/env bash
set -e

if [ ! -e pid ]; then
  echo "'pid' file is absent, something was broken"
  exit 1
fi

pid=$(cat pid)
if ps -p "${pid}" >/dev/null; then
  exit
fi

if [ ! -e cid ]; then
  echo "'cid' file is absent, container wasn't started correctly"
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

