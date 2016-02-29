#!/bin/sh

if (grep -r "Copyright[[:space:]]\+(c)[[:space:]]\+2009-.*rultor.com" \
  --exclude-dir "target" --exclude-dir ".git" --exclude "years.sh" . \
  | grep -v 2009-`date +%Y`) then
    echo "Files above have wrong years in copyrights"
    exit 1
fi

if (grep -r -L "Copyright[[:space:]]\+(c)[[:space:]]\+2009-.*rultor.com" \
 --include=*.{java,xml,vm,groovy,txt,fml,properties} --exclude-dir "target" . \
 | grep -v 2009-`date +%Y`) then
    echo "Files above must have copyright block in header"
    exit 1
fi
