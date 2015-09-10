set -e
set -x

if [ ! -e cid ]; then
  exit 0
fi
cid=$(cat cid)
if docker ps -qa | grep --quiet "${cid}"; then
  docker stop "${cid}"
  docker kill "${cid}"
fi
