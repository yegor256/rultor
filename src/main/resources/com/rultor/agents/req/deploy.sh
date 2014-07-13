#!/bin/sh

cd ..
sudo docker run --rm -v $(pwd):/main "${vars[@]}" -w=/main ${image} /main/${bin}
