#!/usr/bin/env bash
# Copyright (c) 2009-2024 Yegor Bugayenko
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met: 1) Redistributions of source code must retain the above
# copyright notice, this list of conditions and the following
# disclaimer. 2) Redistributions in binary form must reproduce the above
# copyright notice, this list of conditions and the following
# disclaimer in the documentation and/or other materials provided
# with the distribution. 3) Neither the name of the rultor.com nor
# the names of its contributors may be used to endorse or promote
# products derived from this software without specific prior written
# permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
# NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
# FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
# THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
# STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
# OF THE POSSIBILITY OF SUCH DAMAGE.

command -v git >/dev/null 2>&1 || { echo "Rultor requires Git to be installed on the SSH host, but the git command was not found. Aborting ..."; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "Rultor requires Docker to be installed on the SSH host, but the docker command was not found. Aborting ..."; exit 1; }
command -v sudo >/dev/null 2>&1 || { echo "Rultor requires Sudo to be installed on the SSH host, but the sudo command was not found. Aborting ..."; exit 1; }
if ! sudo -n true 2>/dev/null
 then
    echo "Rultor requires passwordless sudo on the SSH host, but this host requires a password. Aborting ..."; exit 1;
fi

docker info >/dev/null 2>&1 || sudo -n service docker restart >/dev/null 2>&1  || { echo >&2 "Rultor requires the Docker cli client to be connected to a working Docker daemon. The Docker cli client on this host appears to not be connected to a working daemon! Aborting ..."; exit 1; }
