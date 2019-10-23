#!/bin/bash
set -e

images=$(docker images --no-trunc | grep -E '([0-9]{2,} weeks|[0-9]+ months) ago' | tr -s ' ' | cut -d ' ' -f 3 | uniq)
for i in ${images}; do
  containers=$(docker ps --filter="ancestor=${i}" | wc -l)
  if [ "${containers}" -eq "1" ]; then
    # echo "Docker image ${i} is being used, won't delete"
  else
      docker rmi -f "${i}" || echo "Failed to remove Docker image ${i}"
  fi
done
