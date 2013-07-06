## How to configure AWS auto scaling group

Create a new EC2 instance (Ubuntu Server 12.x) and install prerequisites there:

```
$ sudo apt-get install openjdk-7-jdk zip unzip
```

Download and unzip Apache Maven 3.0.x into `/usr/share/apache-maven`.

Add this line to `/etc/rsyslog.conf` (replace `XXXXX` with a real port
number provided by PapertrailApp):

```
*.* @logs.papertrailapp.com:XXXXX
```

Create `/home/ubuntu/start.local` so that it contains (and make sure
it is executable):

```
#/bin/bash
export M2_HOME="/usr/local/share/apache-maven"
export PATH="${M2_HOME}/bin:${PATH}"
export MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
ARGS=`curl --silent http://169.254.169.254/latest/user-data`
mvn test -U $ARGS
if [ $? -eq 0 ]; then
  echo "valid reboot"
  sudo shutdown -h now
fi
echo "invalid termination of conveyer"
```

Add this line to crontab:

```
@reboot ~/start.sh 2>&1 | logger -t rultor
```

Create `/home/ubuntu/pom.xml` as provided below.

Remove `/home/ubuntu/.ssh/authorized_keys` file.

Create AMI from this EC2 instance and terminate the instance.

Install AWS Auto Scaling [command line tools](http://aws.amazon.com/developertools/2535).

Create new AWS Auto Scaling launch configuration ([more info](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/US_BasicSetup.html)):

```
$ as-create-launch-config rultor-conveyer \
  --image-id ami-b23a46db --instance-type t1.micro \
  --group rultor-conveyer --monitoring-disabled \
  --user-data "-Daws-key=.. -Daws-secret=.. -Ddynamo-prefix=.. -Dsqs-url=.."
```

Meaning of these Maven command line arguments is explained in
`rultor-queue` and `rultor-users` modules.

Create Auto Scaling group:

```
$ as-create-auto-scaling-group rultor-conveyer \
  --launch-configuration rultor-conveyer \
  --availability-zones us-east-1d --min-size 1 --max-size 2 \
  --desired-capacity 1
```

AWS should launch the first EC2 instance automatically. That's it.

### `pom.xml` for EC2 instance

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.rultor</groupId>
    <artifactId>conveyer</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>
    <prerequisites>
        <maven>3.0</maven>
    </prerequisites>
    <repositories>
        <repository>
            <id>oss.sonatype.org</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
        </repository>
    </repositories>
    <dependencies>
        <dependency>
            <groupId>com.rultor</groupId>
            <artifactId>rultor-conveyer</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>1.2.1</version>
                <executions>
                    <execution>
                        <id>execute</id>
                        <phase>test</phase>
                        <goals>
                            <goal>java</goal>
                        </goals>
                        <configuration>
                            <mainClass>com.rultor.conveyer.Main</mainClass>
                            <arguments>
                                <arg>--sqs-key=${aws-key}</arg>
                                <arg>--sqs-secret=${aws-secret}</arg>
                                <arg>--sqs-url=${sqs-url}</arg>
                                <arg>--dynamo-key=${aws-key}</arg>
                                <arg>--dynamo-secret=${aws-secret}</arg>
                                <arg>--dynamo-prefix=${dynamo-prefix}</arg>
                            </arguments>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
