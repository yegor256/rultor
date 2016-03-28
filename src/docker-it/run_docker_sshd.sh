#!/bin/bash
# requires command line argument with path to rultor root folder
if ($1); then
    echo "Required path to rultor root folder"
    exit 1
fi
echo "Path to rultor root folder is $1"
dockerit=$1/src/docker-it
container=rultor_sshd
image="${container}_img"
echo "Image=$image"
echo "Container=$container"
echo "Grant 600 permissions for keys"
chmod 600 "$dockerit/id_rsa"
chmod 600 "$dockerit/id_rsa.pub"
echo "Building docker image $image"
docker build -t $image "$dockerit"
echo "Removing docker container $container"
docker stop "$container"
docker rm "$container"
echo "Running docker container $container"
docker run --name "$container" -d -p 22 "$image"
echo "Exporting container port"
rm "$1/target/sshd.port"
docker inspect --format='{{(index (index .NetworkSettings.Ports "22/tcp") 0).HostPort}}' "$container" >> "$1/target/sshd.port"
echo "Exporting docker-machine host ip"
rm "$1/target/sshd.host"
docker-machine ip >> "$1/target/sshd.host"
