#!/usr/bin/env bash
set -e
set -x

if [ ! -e cid ]; then
  exit 0
fi
cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  docker stop "${cid}"
  docker kill "${cid}"
  rm -f cid
else
  exit 1
fi

