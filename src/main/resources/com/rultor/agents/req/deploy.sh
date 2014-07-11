#!/bin/sh
if [ -z "${scripts}" ]; then
  echo "deploy.script is not defined in .rultor.yml"
  exit -1
fi

git clone "${head}" repo
cd repo
git checkout "${branch}"
cd ..

sudo docker run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}

cd repo
