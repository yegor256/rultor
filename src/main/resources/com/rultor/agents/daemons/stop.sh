#!/usr/bin/env bash
set -e

if [ ! -e cid ]; then
  exit
fi
cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  docker stop "${cid}"
  docker kill "${cid}"
  rm -f cid
fi

