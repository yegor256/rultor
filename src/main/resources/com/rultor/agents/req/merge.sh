#!/bin/sh

cd repo
git remote add fork "${fork}"
git remote update
if [ "${squash}" == "true" ]; then
  git merge --squash -m "${pull_title}" "fork/${fork_branch}"
else
  git merge "fork/${fork_branch}"
fi

docker_when_possible

git push origin ${head_branch}
