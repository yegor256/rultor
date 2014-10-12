#!/bin/sh

git clone --branch="${head_branch}" --depth=10 "${head}" repo
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
  adduser --disabled-password --gecos '' r
  adduser r sudo
  echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers
  cp -R /root/* /home/r
  cp -R ./* /home/r
  rm -rf repo
  chown -R r:r /home/r
  chmod a+x /home/r/script.sh
  su -m r -c /home/r/script.sh
  mv /home/r/repo .
  chown -R \$(whoami) repo
EOT
fi
chmod a+x entry.sh
cat <<EOT > script.sh
#!/bin/bash
set -x
set -e
set -o pipefail
export HOME=/home/r
cd /home/r/repo
EOT
echo "${scripts[@]}" >> script.sh

function docker_when_possible {
  while true; do
    load=$(uptime | awk '{print $12}' | cut -d ',' -f 1)
    if [ `echo $load \> 8 | bc` -eq 1 ]; then
      echo "load average is ${load}, too high to run a new Docker container"
      echo "I will try again in 15 seconds"
      sleep 15
    else
      echo "load average is ${load}, low enough to run a new Docker container"
      break
    fi
  done
  cd ..
  docker pull "${image}"
  docker run --rm -v "$(pwd):/main" "${vars[@]}" \
    --memory=4g "--cidfile=$(pwd)/cid" -w=/main "${image}" /main/entry.sh
  sudo chown -R $(whoami) repo
  cd repo
}
