#!/bin/bash
# requires command line argument with path to Rultor root folder
if [ -z "$1" ]; then
    echo "Required path to Rultor root folder not given!"
    exit 1
fi
if [ -z "$2" ]; then
    echo "Required Docker host not given!"
    exit 1
fi
echo "Path to rultor root folder is ${1}"
dockerit="${1}/src/docker-it"
image="rultor-sshd-it"
chmod 600 "${dockerit}/id_rsa"
chmod 600 "${dockerit}/id_rsa.pub"
docker -H $2 build -t "${image}" "${dockerit}"
mkdir -p ${1}/target/docker-it
bash -l ${dockerit}/clear_docker_sshd.sh ${1} 2>/dev/null
docker -H $2 run -d --name="$(dd if=/dev/urandom bs=10k count=1 2>/dev/null | tr -cd 'a-z0-9' | head -c 8)" \
 --cidfile="${1}/target/docker-it/cid"  -p 22 "${image}"
rm -f "${1}/target/docker-it/sshd.port"
docker -H $2 inspect --format='{{(index (index .NetworkSettings.Ports "22/tcp") 0).HostPort}}' \
    "$(<${1}/target/docker-it/cid)" >> "${1}/target/docker-it/sshd.port"
echo "$(<${1}/target/docker-it/sshd.port)"
