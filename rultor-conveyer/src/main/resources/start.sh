#!/bin/bash

export M2_HOME="/usr/local/share/apache-maven"
export PATH="${M2_HOME}/bin:/usr/local/bin:${PATH}"
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

SQS_URL=`curl --silent http://169.254.169.254/latest/user-data | jq -r '.url'`
DYNAMO_PREFIX=`curl --silent http://169.254.169.254/latest/user-data | jq -r '.prefix'`
INSTANCE=`curl --silent http://169.254.169.254/latest/meta-data/instance-id`

# https://github.com/sebdah/dynamic-dynamodb
for table in units receipts statements
do
    dynamic-dynamodb --daemon start --instance "${table}" \
        --region us-east-1 \
        --table-name "${DYNAMO_PREFIX}${table}" \
        --reads-upper-threshold 90 \
        --reads-lower-threshold 30 \
        --increase-reads-with 50 \
        --decrease-reads-with 40 \
        --writes-upper-threshold 90 \
        --writes-lower-threshold 40 \
        --increase-writes-with 40 \
        --decrease-writes-with 70 \
        --check-interval 300
done

curl --silent https://raw.github.com/rultor/rultor/master/rultor-conveyer/src/main/resources/ec2-pom.xml > pom.xml
mvn test --quiet --update-snapshots \
    "-Dsqs-url=${SQS_URL}" "-Ddynamo-prefix=${DYNAMO_PREFIX}"

if [ $? -eq 0 ]; then
    ec2-terminate-instances "${INSTANCE}"
fi
