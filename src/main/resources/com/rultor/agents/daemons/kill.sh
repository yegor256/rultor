#!/usr/bin/env bash
set -e
set -x

echo "trying to kill Docker container due to timeout..."

if [ -e cid ]; then
  cid=$(cat cid)
  if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
    docker rm -f "${cid}"
    echo "container ${cid} killed"
  else
    echo "container ${cid} is dead already"
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
  echo "QUIT signal sent to process #${pid}"
else
  echo "process #${pid} is dead already, no need to QUIT"
fi

sleep 15
if [ -n "$(ps -p $pid -opid=)" ]; then
  kill -9 "${pid}"
  echo "process #${pid} killed"
else
  echo "process #${pid} is dead already, no need to KILL"
fi
rm -f pid
