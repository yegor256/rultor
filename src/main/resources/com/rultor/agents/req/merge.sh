if [ -z "${SCRIPT}" ]; then
  if [ -e pom.xml ]; then
    SCRIPT="mvn help:system clean install --batch-mode --errors"
  fi
  if [ -e build.xml ]; then
    SCRIPT="ant"
  fi
  if [ -e Gemfile ]; then
    SCRIPT="bundle test"
  fi
  if [ -z "${SCRIPT}" ]; then
    echo "merge.script is not defined in .rultor.yml and project root directory doesn't explain what this project is built with"
    exit -1
  fi
fi

git clone "${base}" repo
cd repo
git config user.email "me@rultor.com"
git config user.name "rultor"
git checkout "${base_branch}"
git remote add head "${head}"
git remote update
git merge "head/${head_branch}"
cd ..

BIN=merge.sh
echo "#!/bin/bash" > ${BIN}
echo "set -x" >> ${BIN}
echo "set -e" >> ${BIN}
echo "set -o pipefail" >> ${BIN}
echo "cd repo" >> ${BIN}
echo "${SCRIPT[@]}" >> ${BIN}
chmod a+x ${BIN}

sudo docker run --rm -v $(pwd):/main "${DOCKER_ENVS[@]}" -w=/main yegor256/rultor /main/${BIN}

cd repo
git push origin "${base_branch}"
