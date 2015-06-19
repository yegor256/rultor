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
git merge ${args} "fork/${fork_branch}"

docker_when_possible

git push origin ${head_branch}
