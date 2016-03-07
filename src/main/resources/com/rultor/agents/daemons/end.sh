#!/usr/bin/env bash
#@todo #754:30min Remove the deprecation code from the shell.
# Move the code to Java. May be
# \main\java\com\rultor\agents\daemons\EndsDaemon.java is
# the place to implement this.

set -e

if [ ! -e pid ]; then
  echo "'pid' file is absent, something was broken"
  exit 1
fi

pid=$(cat pid)
if ps -p "${pid}" >/dev/null; then
  exit
fi

if [ ! -e cid ]; then
  echo "'cid' file is absent, container wasn't started correctly"
  exit 1
fi

cid=$(cat cid)
if docker ps -qa --no-trunc | grep --quiet "${cid}"; then
  echo "container ${cid} is alive"
else
  echo "container ${cid} is dead"
  date
  if $image = "yegor256/rultor"; then
    deprecation
  fi
  exit 1
fi
