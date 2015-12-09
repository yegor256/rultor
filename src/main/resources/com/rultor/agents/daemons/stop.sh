#!/usr/bin/env bash
set -e
set -x

echo "Trying to stop running Docker container..."

if [ ! -e cid ]; then
  exit 0
fi
cid=$(cat cid)
if docker ps -qa | grep --quiet "${cid}"; then
  docker stop "${cid}"
  docker kill "${cid}"
else
  echo "Container ${cid} not found"
  exit -1
fi

