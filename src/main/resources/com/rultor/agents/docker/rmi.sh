#!/bin/bash
set -e

flag=/tmp/rultor-rmi-running

if [ -e "${flag}" ]; then
  echo This script is currently running, no need to run again
  exit
fi

trap 'rm ${flag}' EXIT

images=$(docker images --no-trunc | grep -E '([0-9]{2,} weeks|[0-9]+ months) ago' | tr -s ' ' | cut -d ' ' -f 3 | uniq)
for i in ${images}; do
  containers=$(docker ps --filter="ancestor=${i}" | wc -l)
  if [ "${containers}" -eq "1" ]; then
    docker rmi -f "${i}" || echo "Failed to remove Docker image ${i}"
  fi
done
