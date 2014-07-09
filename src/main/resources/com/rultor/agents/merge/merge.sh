git clone "${BASE}" repo
cd repo
git checkout "${BASE_BRANCH}"
git remote add head "${HEAD}"
git remote update
git config user.email "me@rultor.com"
git config user.name "rultor"
git merge "head/${HEAD_BRANCH}"
cd ..

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

BIN=merge.sh
echo "#!/bin/bash" > ${BIN}
echo "cd repo" >> ${BIN}
echo "${SCRIPT[@]}" >> ${BIN}
chmod a+x ${BIN}

sudo docker run --rm -v $(pwd):/main "${DOCKER_ENVS[@]}" -w=/main yegor256/rultor /main/${BIN}

cd repo
git push origin "${BASE_BRANCH}"
