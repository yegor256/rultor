#!/bin/sh
if [ -z "${SCRIPT}" ]; then
  echo "deploy.script is not defined in .rultor.yml"
  exit -1
fi

git clone "${head}" repo
cd repo
git checkout "${branch}"
cd ..

sudo docker run --rm -v $(pwd):/main "${DOCKER_ENVS[@]}" -w=/main yegor256/rultor /main/${BIN}

cd repo
