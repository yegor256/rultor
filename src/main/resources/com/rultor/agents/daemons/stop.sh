#!/usr/bin/env bash
set -e
set -x

echo "Trying to stop running Docker container..."

if [ ! -e cid ]; then
  exit 0
fi
cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  docker stop "${cid}"
  docker kill "${cid}"
else
  echo "Container ${cid} not found"
fi

