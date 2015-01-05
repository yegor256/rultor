#!/bin/bash
set -e

cd $(dirname $0)
cp /code/home/rultor/settings.xml .
git add settings.xml
git commit -m 'settings.xml for heroku'
trap 'git reset HEAD~1 && rm settings.xml' EXIT
git push heroku master -f

