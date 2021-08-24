#!/bin/sh

cd repo
git remote add fork "${fork}"
git remote update
args=""
if [ "${ff}" == "default" ]; then
  args="${args} --ff"
fi
if [ "${ff}" == "no" ]; then
  args="${args} --no-ff"
fi
if [ "${ff}" == "only" ]; then
  args="${args} --ff-only"
fi

export BRANCH=__rultor
while [ $(git show-branch "${BRANCH}" 2>/dev/null | wc -l) -gt 0 ]; do
  export BRANCH="__rultor-$(cat /dev/urandom | tr -cd 'a-z0-9' | head -c 16)"
done

git status
git checkout -B "${BRANCH}" "fork/${fork_branch}"
git checkout -B "${head_branch}" "origin/${head_branch}"

if [ "${rebase}" == "true" ]; then
  git checkout "${BRANCH}"
  git rebase "${head_branch}"
  git checkout "${head_branch}"
fi

if [ "${squash}" == "true" ]; then
  git merge ${args} --squash "${BRANCH}"
  git commit -m "${pull_title}"
else
  git merge ${args} "${BRANCH}"
fi

docker_when_possible

git push origin "${head_branch}"
