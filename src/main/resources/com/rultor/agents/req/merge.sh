#!/bin/sh

cd repo
git remote add fork "${fork}"
git remote update
args=""
if [ "${squash}" == "true" ]; then
  args="${args} --squash -m \"${pull_title}\""
fi
if [ "${ff}" == "default" ]; then
  args="${args} --ff"
fi
if [ "${ff}" == "no" ]; then
  args="${args} --no-ff"
fi
if [ "${ff}" == "only" ]; then
  args="${args} --ff-only"
fi

if [ "${rebase}" == "true" ]; then
  git checkout "fork/${fork_branch}"
  git rebase "origin/${head_branch}"
  git checkout "origin/${head_branch}"
fi

git merge ${args} "fork/${fork_branch}"

docker_when_possible

git push origin ${head_branch}
