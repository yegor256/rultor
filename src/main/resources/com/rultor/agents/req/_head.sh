#!/bin/sh

if [ -z "${docker}" ]; then
  docker="docker"
fi

git clone "${head}" repo
cd repo
git config user.email "me@rultor.com"
git config user.name "rultor"
git checkout "${head_branch}"

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
adduser --disabled-password --gecos '' r
adduser r sudo
cp -R ./* /home/r
rm -rf repo
chown -R r /home/r
chmod a+x script.sh
su r -c /home/r/script.sh
mv /home/r/repo .
chown -R $(whoami) ./repo
EOT
chmod a+x entry.sh
echo "#!/bin/bash" > script.sh
echo "cd ~/repo" >> script.sh
echo "${scripts[@]}" >> script.sh
cd repo

function docker_when_possible {
  while true; do
    load=$(uptime | awk '{print $12}' | cut -d ',' -f 1)
    if [ "${load}" \> 8 ]; then
      echo "load average is ${load}, too high to run a new Docker container"
      echo "I will try again in 15 seconds"
      sleep 15
    else
      echo "load average is ${load}, low enough to run a new Docker container"
      break
    fi
  done
  cd ..
  ${docker} run --rm -v "$(pwd):/main" "${vars[@]}" \
    --memory=4g "--cidfile=$(pwd)/cid" -w=/main "${image}" /main/entry.sh
  cd repo
}
