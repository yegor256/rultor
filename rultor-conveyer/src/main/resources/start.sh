#!/bin/bash -x

export M2_HOME="/usr/local/share/apache-maven"
export PATH="${M2_HOME}/bin:${PATH}"
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"

curl --silent http://169.254.169.254/latest/user-data > /tmp/credentials.sh
source /tmp/credentials.sh
echo "Loaded access credentials from EC2"

INSTANCE=`curl --silent http://169.254.169.254/latest/meta-data/instance-id`
echo "Currently running instance is ${INSTANCE}"

curl --silent https://raw.github.com/yegor256/rultor/master/rultor-conveyer/src/main/resources/ec2-pom.xml > pom.xml
echo "pom.xml loaded from Github"

mvn test --quiet --update-snapshots \
    "-Daws-key=${AWS_KEY}" "-Daws-secret=${AWS_SECRET}" \
    "-Dsqs-url=${SQS_URL}" "-Ddynamo-prefix=${DYNAMO_PREFIX}"
CODE=$?
echo "Maven execution finished with ${CODE}"

if [ $CODE -eq 0 ]; then
    echo "EC2 instance is going to terminate itself..."
    ec2-terminate-instances --aws-access-key "${AWS_KEY}" \
        --aws-secret-key "${AWS_SECRET}" "${INSTANCE}"
fi
echo "Something went wrong in Maven, see logs above"
