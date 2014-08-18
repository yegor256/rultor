#!/bin/sh

cd repo
git remote add fork "${fork}"
git remote update
git merge "fork/${fork_branch}"

docker_when_possible

git push origin "${head_branch}"
