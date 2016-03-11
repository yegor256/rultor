#!/usr/bin/env bash

command -v git >/dev/null 2>&1 || { echo "Rultor requires Git to be installed on the SSH host, but the git command was not found. Aborting ..."; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "Rultor requires Docker to be installed on the SSH host, but the docker command was not found. Aborting ..."; exit 1; }
command -v sudo >/dev/null 2>&1 || { echo "Rultor requires Sudo to be installed on the SSH host, but the sudo command was not found. Aborting ..."; exit 1; }
if ! sudo -n true 2>/dev/null
 then
    echo "Rultor requires passwordless sudo on the SSH host, but this host requires a password. Aborting ..."; exit 1;
fi

docker info >/dev/null 2>&1 || sudo -n service docker restart >/dev/null 2>&1  || { echo >&2 "Rultor requires the Docker cli client to be connected to a working Docker daemon. The Docker cli client on this host appears to not be connected to a working daemon! Aborting ..."; exit 1; }
