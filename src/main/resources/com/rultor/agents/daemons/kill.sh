#!/usr/bin/env bash
set -e
set -x

echo "Trying to kill Docker container due to timeout..."

if [ -e cid ]; then
  cid=$(cat cid)
  if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
    docker rm -f "${cid}"
    echo "Docker container ${cid} killed"
  fi
  rm -f cid
fi
if [ ! -e pid ]; then
  echo "'pid' file doesn't exist"
  exit 0
fi
pid=$(cat pid)
if [ -n "$(ps -p $pid -opid=)" ]; then
  kill "${pid}"
  echo "Process #${pid} quit"
fi
sleep 15
if [ -n "$(ps -p $pid -opid=)" ]; then
  kill -9 "${pid}"
  echo "Process #${pid} killed"
fi
rm -f pid
