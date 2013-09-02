#!/bin/bash
#
# Copyright (c) 2009-2013, rultor.com
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

# add swap disk
sudo dd if=/dev/zero of=/swapfile bs=1024 count=1048576
sudo /sbin/mkswap /swapfile
sudo chown root:root /swapfile
sudo chmod 0600 /swapfile
sudo /sbin/swapon /swapfile

export M2_HOME="/usr/local/share/apache-maven"
export PATH="${M2_HOME}/bin:/usr/local/bin:${PATH}"
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

SQS_URL=`curl --silent http://169.254.169.254/latest/user-data | jq -r '.url'`
SQS_WALLET_URL=`curl --silent http://169.254.169.254/latest/user-data | jq -r '.wallet_url'`
DYNAMO_PREFIX=`curl --silent http://169.254.169.254/latest/user-data | jq -r '.prefix'`
PGSQL_URL=`curl --silent http://169.254.169.254/latest/user-data | jq -r '.pgsql_url'`
PGSQL_PASSWORD=`curl --silent http://169.254.169.254/latest/user-data | jq -r '.pgsql_password'`
INSTANCE=`curl --silent http://169.254.169.254/latest/meta-data/instance-id`

# to update the version of dynamic-dynamo
sudo pip install -U dynamic-dynamodb

# https://github.com/sebdah/dynamic-dynamodb
dynamic-dynamodb --version
for table in rules stands
do
    while true
    do
        dynamic-dynamodb --log-level WARNING \
            --table-name "${DYNAMO_PREFIX}${table}" \
            --increase-reads-with 1 \
            --decrease-reads-with 1 \
            --increase-writes-with 1 \
            --decrease-writes-with 1 \
            --increase-reads-unit units \
            --decrease-reads-unit units \
            --reads-upper-threshold 90 \
            --reads-lower-threshold 30 \
            --writes-upper-threshold 90 \
            --writes-lower-threshold 40 \
            --min-provisioned-reads 1 \
            --max-provisioned-reads 32 \
            --min-provisioned-writes 1 \
            --max-provisioned-writes 32
        sleep 300
    done &
done

curl --silent https://raw.github.com/rultor/rultor/master/rultor-conveyer/src/main/resources/ec2-pom.xml > pom.xml
mvn test --batch-mode --strict-checksums --quiet --update-snapshots \
    "-Dsqs-url=${SQS_URL}" \
    "-Dsqs-wallet-url=${SQS_WALLET_URL}" \
    "-Ddynamo-prefix=${DYNAMO_PREFIX}" \
    "-Dpgsql-url=${PGSQL_URL}" \
    "-Dpgsql-password=${PGSQL_PASSWORD}"

ec2-terminate-instances "${INSTANCE}"
