#!/bin/sh

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request, see http://doc.rultor.com/basics.html"
  exit -1
fi

cd repo
if [ $(git tag -l "${tag}") ]; then
   echo "Tag ${tag} already exists!"
   exit -1
fi

export BRANCH_NAME=

while [ `git branch --list $BRANCH_NAME | wc -l` -gt 0 ]; do
    export BRANCH_NAME="__rultor-tmp-`cat /dev/urandom | tr -cd 'a-z0-9' | head -c 16`"
done
git checkout -b $BRANCH_NAME


docker_when_possible

git tag "${tag}" -m "${tag}: tagged by rultor.com"
git push origin "${tag}"
