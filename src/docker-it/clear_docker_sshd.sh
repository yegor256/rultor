#!/bin/bash
container="rultor_sshd"
image="${container}_img"
echo "Removing docker container $container"
docker stop "$container"
docker rm "$container"
