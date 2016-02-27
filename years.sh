#!/bin/sh

if (grep -r "Copyright[[:space:]]\+(c)[[:space:]]\+2009-.*rultor.com" --exclude-dir ".git" --exclude "years.sh" . | grep -v 2009-`date +%Y`) then
    echo "Files above have wrong years in copyrights"
    exit 1
fi

