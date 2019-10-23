#!/bin/bash
set -e

images=$(docker images | grep -E '([0-9]{2,} weeks|[0-9]+ months) ago' | tr -s ' ' | cut -d ' ' -f 3 | uniq)
for i in ${images}; do
  docker rmi -f "${i}" || echo "Failed to remove Docker image ${i}"
done
