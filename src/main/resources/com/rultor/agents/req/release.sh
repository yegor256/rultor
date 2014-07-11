#!/bin/sh
if [ -z "${scripts}" ]; then
  echo "release.script is not defined in .rultor.yml"
  exit -1
fi

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request"
  exit -1
fi

git clone "${head}" repo
cd repo
git config user.email "me@rultor.com"
git config user.name "rultor"
git checkout "${branch}"

sudo docker run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}

git commit --allow-empty -am "${tag}"
git tag "${tag}"
git push origin "${tag}"
git reset HEAD~1
git reset --hard
git push origin ${branch}
