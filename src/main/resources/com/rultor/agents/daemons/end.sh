#!/usr/bin/env bash
set -e
set -x

if [ ! -e pid ]; then
  echo "'pid' file not found, most likely the process is dead already"
  exit 1
fi

pid=$(cat pid)
if [ ps -p "${pid}" >/dev/null ]; then
  echo "Process #${pid} doesn't exist any more"
  exit 1
fi

if [ ! -e cid ]; then
  echo "'cid' file not found"
  exit 0
fi
cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  echo "Container ${cid} is alive"
else
  echo "Docker container ${cid} is dead"
  exit 1
fi

