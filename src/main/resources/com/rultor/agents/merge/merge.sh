git clone "${BASE}" repo
cd repo
git checkout "${BASE-BRANCH}"
git remote add head "${HEAD}"
git remote update
git merge "head/${HEAD-BRANCH}"

if [ -e pom.xml ]; then
  CMD="mvn help:system clean install --batch-mode --update-snapshots --errors --strict-checksums"
fi
if [ -e build.xml ]; then
  CMD="ant"
fi
if [ -e Gemfile ]; then
  CMD="bundle test"
fi

sudo docker run --rm -v $(pwd):/main -w=/main yegor256/rultor "${SCRIPT}"
git push origin "${BASE-BRANCH}"
