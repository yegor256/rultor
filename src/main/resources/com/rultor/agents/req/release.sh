#!/bin/sh

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request, see http://doc.rultor.com/basics.html"
  exit -1
fi

if [ $(git tag -l "${tag}") ]; then
   echo "Tag ${tag} already exists!"
   exit -1
fi

git checkout -b __rultor-tmp

docker_when_possible

git tag "${tag}" -m "${tag}: tagged by rultor.com"
git push origin "${tag}"
