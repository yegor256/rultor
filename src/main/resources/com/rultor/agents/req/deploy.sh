#!/bin/sh

cd ..
${sudo} docker_when_possible run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}
