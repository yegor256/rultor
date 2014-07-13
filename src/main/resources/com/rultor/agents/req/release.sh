#!/bin/sh

if [ -z "${tag}" ]; then
  echo "tag name is not provided in the request, see http://doc.rultor.com/basics.html"
  exit -1
fi

git checkout -b __rultor-tmp

cd ..
${sudo} docker run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}
cd repo

${sudo} git commit --allow-empty -am "${tag}"
${sudo} git tag "${tag}"
${sudo} git push origin "${tag}"
