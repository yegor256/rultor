#!/bin/bash
# requires command line argument with path to Rultor root folder
if [ -z "$1" ]; then
    echo "Required path to Rultor root folder"
    exit 1
fi
if [ -z "$2" ]; then
    echo "Required Docker host not given!"
    exit 1
fi
echo "Removing Docker container $(<$1/target/docker-it/cid)"
docker -H $2 rm -f "$(<$1/target/docker-it/cid)"
rm -f $1/target/docker-it/cid
