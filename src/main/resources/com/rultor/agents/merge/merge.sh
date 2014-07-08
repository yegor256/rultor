git clone "${BASE}" repo
cd repo
git checkout "${BASE_BRANCH}"
git remote add head "${HEAD}"
git remote update
git merge "head/${HEAD_BRANCH}"

if [ -z "${SCRIPT}" ]; then
  if [ -e pom.xml ]; then
    SCRIPT="mvn help:system clean install --batch-mode --update-snapshots --errors --strict-checksums"
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

sudo docker run --rm -v $(pwd):/main -w=/main yegor256/rultor ${SCRIPT}
git push origin "${BASE_BRANCH}"
