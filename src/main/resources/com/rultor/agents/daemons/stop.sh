set -e
set -x

if [ ! -e cid ]; then
  exit 0
fi
cid=$(cat cid)
docker stop "${cid}"
rm cid
