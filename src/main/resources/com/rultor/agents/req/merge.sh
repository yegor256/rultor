#!/bin/sh

git remote add fork "${fork}"
git remote update
git merge "fork/${fork_branch}"

cd ..
${sudo} docker run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}
cd repo

${sudo} git push origin "${head_branch}"
