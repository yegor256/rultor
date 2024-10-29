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

if (grep -r "Copyright \+(c) \+2009-.*rultor.com" \
  --exclude-dir "target" --exclude-dir ".git" --exclude "years.sh" \
  --exclude-dir ".idea" --exclude "settings.xml" \
  --exclude-dir "apache-maven-*" . | grep -v 2009-`date +%Y`); then
    echo "Files above have wrong years in copyrights"
    exit 1
fi

if (grep -r -L "Copyright \+(c) \+2009-.*Yegor Bugayenko" \
  --include=*.java --include=*.xml --include=*.vm --include=*.groovy \
  --include=*.txt --include=*.fml --include=*.properties} \
  --exclude-dir "target" --exclude-dir "apache-maven-*" . \
  --exclude-dir ".idea" --exclude "settings.xml" \
  | grep -v 2009-`date +%Y`); then
    echo "Files above must have copyright block in header"
    exit 1
fi
