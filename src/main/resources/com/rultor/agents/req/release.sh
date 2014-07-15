#!/bin/sh

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request, see http://doc.rultor.com/basics.html"
  exit -1
fi

if [ $(git tag -l "${tag}") ]
then
   echo "Tag ${tag} already exists!"
   exit -1
fi

git checkout -b __rultor-tmp

cd ..
docker_when_possible run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}
${sudo} chown -R $(whoami) .
cd repo

git tag "${tag}"
git push origin "${tag}"
