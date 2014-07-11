#!/bin/sh
if [ -z "${scripts}" ]; then
  echo "merge.script is not defined in .rultor.yml and project root directory doesn't explain what this project is built with"
  exit -1
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

sudo docker run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}

cd repo
git push origin "${base_branch}"
