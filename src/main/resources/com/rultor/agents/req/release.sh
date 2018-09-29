#!/bin/sh

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request, see http://doc.rultor.com/basics.html"
  exit -1
fi

if [[ "${tag}" =~ ^[a-zA-Z0-9\\.\\-]+$ ]]; then
  echo "tag name is valid: \"${tag}\""
else
  echo "tag name contains invalid characters: \"${tag}\""
  exit -1
fi

cd repo
if [ $(git tag -l "${tag}") ]; then
  echo "Tag ${tag} already exists!"
  exit -1
fi

export BRANCH_NAME=__rultor
while [ $(git show-branch "${BRANCH_NAME}" 2>/dev/null | wc -l) -gt 0 ]; do
  export BRANCH_NAME="__rultor-$(cat /dev/urandom | tr -cd 'a-z0-9' | head -c 16)"
done
git checkout -b "${BRANCH_NAME}"

docker_when_possible

for f in "${sensitive[@]}"; do
  if [ -e "${f}" ]; then
    echo "Sensitive file ${f} is present, can't release"
    exit - 1
  fi
  echo "Sensitive file ${f} is absent, we are good"
done

git checkout "${BRANCH_NAME}"
git tag "${tag}" -m "${tag}: tagged by rultor.com"
git reset --hard
git clean -fd
git checkout master
git branch -D "${BRANCH_NAME}"
git push --all origin
git push --tags origin
