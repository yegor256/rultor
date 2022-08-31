#!/bin/bash
set -e

# Here we find the oldest Docker images on the sever
# and delete them one by one. At the moment, we delete
# only the first one, because deleting them all may
# be a very time consuming operation, while this script is NOT
# running in the background.

images=$(docker images --no-trunc | grep -E '([0-9]{2,} weeks|[0-9]+ months) ago' | tr -s ' ' | cut -d ' ' -f 3 | uniq | head -1)
for i in ${images}; do
  containers=$(docker ps --filter="ancestor=${i}" | wc -l)
  if [ "${containers}" -eq "1" ]; then
    docker rmi -f "${i}" || echo "Failed to remove Docker image ${i}"
  fi
done
