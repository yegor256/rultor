#!/bin/sh

if (grep -r "Copyright \+(c) \+2009-.*rultor.com" \
  --exclude-dir "target" --exclude-dir ".git" --exclude "years.sh" . \
  | grep -v 2009-`date +%Y`); then
    echo "Files above have wrong years in copyrights"
    exit 1
fi

if (grep -r -L "Copyright \+(c) \+2009-.*rultor.com" \
 --include=*.java --include=*.xml --include=*.vm --include=*.groovy \
 --include=*.txt --include=*.fml --include=*.properties} \
 --exclude-dir "target" . | grep -v 2009-`date +%Y`); then
    echo "Files above must have copyright block in header"
    exit 1
fi
