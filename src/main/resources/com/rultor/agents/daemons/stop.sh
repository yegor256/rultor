#!/usr/bin/env bash
set -e
set -x

echo "trying to stop running Docker container..."

if [ ! -e cid ]; then
  exit 0
fi
cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  docker stop "${cid}"
  echo "container ${cid} stopped"
  docker kill "${cid}"
  echo "container ${cid} killed"
  rm -f cid
  echo "'cid' file removed"
else
  echo "container ${cid} not found"
  exit 1
fi

