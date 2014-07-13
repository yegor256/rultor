#!/bin/sh

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request, see http://doc.rultor.com/basics.html"
  exit -1
fi

git checkout -b __rultor-tmp

cd ..
${docker} run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}
cd repo

git commit --allow-empty -am "${tag}"
git tag "${tag}"
git push origin "${tag}"
