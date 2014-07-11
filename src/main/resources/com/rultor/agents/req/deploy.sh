git clone "${head}" repo
cd repo
git checkout "${branch}"
cd ..

if [ -z "${SCRIPT}" ]; then
  echo "deploy.script is not defined in .rultor.yml"
  exit -1
fi

BIN=deploy.sh
echo "#!/bin/bash" > ${BIN}
echo "set -x" >> ${BIN}
echo "set -e" >> ${BIN}
echo "set -o pipefail" >> ${BIN}
echo "cd repo" >> ${BIN}
echo "${SCRIPT[@]}" >> ${BIN}
chmod a+x ${BIN}

sudo docker run --rm -v $(pwd):/main "${DOCKER_ENVS[@]}" -w=/main yegor256/rultor /main/${BIN}

cd repo
