#!/bin/sh

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
bin=script.sh
echo "#!/bin/bash" > ${bin}
echo "set -x" >> ${bin}
echo "set -e" >> ${bin}
echo "set -o pipefail" >> ${bin}
echo "cd repo" >> ${bin}
echo "${scripts[@]}" >> ${bin}
chmod a+x ${bin}
cd repo

if [ -z "${sudo}" ]; then
  sudo="sudo"
fi

function docker_when_possible {
  while true; do
    load=$(uptime | awk '{print $12}' | cut -d "," -f 1)
    if [ "${load}" -ge "8" ]; then
      echo "load average is ${load}, too high to run a new Docker container"
      echo "I will try again in 15 seconds"
      sleep 15
    else
      echo "load average is ${load}, low enough to run a new Docker container"
      break
    fi
  done
  ${sudo} docker $@
}
