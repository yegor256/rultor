#!/bin/sh
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

hostname
pwd

git --version
docker --version

mkdir -p ~/.ssh
echo -e "Host github.com\n\tStrictHostKeyChecking no\n" > ~/.ssh/config
chmod 600 ~/.ssh/config
git clone "${head}" repo
cd repo
git config user.email "me@rultor.com"
git config user.name "rultor"

if [ -z "${scripts}" ]; then
  if [ -e "pom.xml" ]; then
    scripts=( 'mvn clean test' )
    echo "pom.xml is here, I guess it is Apache Maven"
  elif [ -e "Gemfile" ]; then
    scripts=( 'bundle test' )
    echo "Gemfile is here, I guess it is Bundler"
  elif [ -e "build.xml" ]; then
    scripts=( 'ant' )
    echo "build.xml is here, I guess it is Apache Ant"
  elif [[ -e "build.sbt" || -e "project/Build.scala" ]]; then
    scripts=( 'sbt' )
    echo "build.sbt or project/Build.scala is here, I guess it is Scala SBT"
  else
    echo "I can't guess your build automation tool, see http://doc.rultor.com/basics.html"
    exit -1
  fi
fi

cd ..
cat <<EOT > entry.sh
#!/bin/bash
set -x
set -e
set -o pipefail
EOT
if [ "${as_root}" = "true" ]; then
  cat <<EOT >> entry.sh
  mkdir /home/r
  cp -R ./* /home/r
  rm -rf repo
  chmod a+x /home/r/script.sh
  /home/r/script.sh
  mv /home/r/repo .
EOT
else
  cat <<EOT >> entry.sh
  shopt -s dotglob
  useradd -m -G sudo r
  for g in \$(cat /etc/group | awk  -F  ':' '\$3 > 100 { print \$1 }'); do usermod -a -G "\${g}" r; done
  usermod -s /bin/bash r
  echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
  cp -R /root/* /home/r
  cp -R ./* /home/r
  rm -rf repo
  chown -R r:r /home/r
  chmod a+x /home/r/script.sh
  cat /home/r/script.sh
  su --login r --command /home/r/script.sh
  mv /home/r/repo .
  chown -R "\$(whoami)" repo
EOT
fi
chmod a+x entry.sh
cat <<EOT > script.sh
#!/bin/bash
set -e
set -o pipefail
if [ -e /home/r/.profile ]; then source /home/r/.profile; fi
shopt -s expand_aliases
alias 'sudo=sudo -i'
export HOME=/home/r
cd "\${HOME}/repo"
EOT
echo "${scripts[@]}" >> script.sh

function docker_when_possible {
  while true; do
    load=$(uptime | sed 's/ /\n/g' | tail -n 1)
    if [ `echo $load \> 30 | bc` -eq 1 ]; then
      echo "load average is ${load}, too high to run a new Docker container"
      echo "I will try again in 15 seconds"
      sleep 15
    else
      echo "load average is ${load}, low enough to run a new Docker container"
      break
    fi
  done
  cd ..
  if [ -n "${directory}" ]; then
    use_image="yegor256/rultor-$(dd if=/dev/urandom bs=10k count=1 2>/dev/null | tr -cd 'a-z0-9' | head -c 8)"
    docker build "${directory}" -t "${use_image}"
  else
    use_image="${image}"
    docker pull "${use_image}"
  fi
  if docker ps --filter=status=exited | grep --quiet "\s${container}\s*\$"; then
    docker rm -f "${container}"
  fi
  ls -al .
  docker run -t --rm \
    -v "$(pwd):/main" "${vars[@]}" \
    --hostname=docker --privileged --net=host \
    --dns 8.8.8.8 \
    --memory=8g --memory-swap=16g --oom-kill-disable \
    "--cidfile=$(pwd)/cid" -w=/main \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --name="${container}" "${use_image}" /main/entry.sh
  if [ -n "${directory}" ]; then
    docker rmi "${use_image}"
  fi
  sudo chown -R $(whoami) repo
  cd repo
}
