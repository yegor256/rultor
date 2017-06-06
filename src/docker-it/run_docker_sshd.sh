#!/bin/bash
# requires command line argument with path to rultor root folder
if ($1); then
    echo "Required path to rultor root folder"
    exit 1
fi
echo "Path to rultor root folder is $1"
dockerit=$1/src/docker-it
image="rultor_sshd_img"
echo "Image=$image"
echo "Grant 600 permissions for keys"
chmod 600 "$dockerit/id_rsa"
chmod 600 "$dockerit/id_rsa.pub"
echo "Building docker image $image"
docker build -t $image "$dockerit"
sh $dockerit/clear_docker_sshd.sh "$1"
echo "Running docker container"
docker run -d --cidfile="$1/target/cid" -p 22 "$image"
echo "Exporting container port"
rm "$1/target/sshd.port"
docker inspect --format='{{(index (index .NetworkSettings.Ports "22/tcp") 0).HostPort}}' \
    "$(<$1/target/cid)" >> "$1/target/sshd.port"
echo "$(<$1/target/sshd.port)"
