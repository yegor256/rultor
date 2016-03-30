#!/bin/bash
# requires command line argument with path to rultor root folder
if ($1); then
    echo "Required path to rultor root folder"
    exit 1
fi
echo "Path to rultor root folder is $1"
image="${container}_img"
echo "Removing docker container $(<$1/target/cid)"
docker rm -f "$(<$1/target/cid)"
rm $1/target/cid
