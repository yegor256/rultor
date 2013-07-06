## How to configure AWS Auto Scaling group

Create a new [EC2 instance](http://aws.amazon.com/ec2/)
(Ubuntu Server 12.x) and install prerequisites there:

```
$ sudo apt-get install openjdk-7-jdk zip unzip
```

Download and unzip Apache Maven 3.0.x into `/usr/share/apache-maven`.

Add this line to `/etc/rsyslog.conf` (replace `XXXXX` with a real port
number provided by PapertrailApp):

```
*.* @logs.papertrailapp.com:XXXXX
```

Add this line to crontab:

```
@reboot /bin/bash `curl --silent https://raw.github.com/yegor256/rultor/master/rultor-conveyer/src/main/resources/start.sh` 2>&1 | logger -t rultor
```

Remove `/home/ubuntu/.ssh/authorized_keys` file.

Create AMI from this EC2 instance
([see how](http://docs.aws.amazon.com/AWSToolkitVS/latest/UserGuide/tkv-create-ami-from-instance.html))
and terminate the instance.

Install AWS Auto Scaling [command line tools](http://aws.amazon.com/developertools/2535).

Create `credentials.txt` file, with production data:

```
AWS_KEY="..."
AWS_SECRET="..."
SQS_URL="..."
DYNAMO_PREFIX="..."
```

Create new AWS Auto Scaling launch configuration ([more info](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/US_BasicSetup.html)):

```
$ as-create-launch-config rultor-conveyer-0.1.10 \
  --image-id ami-b23a46db --instance-type t1.micro \
  --group rultor-conveyer --monitoring-disabled \
  --user-data-file credentials.txt
```

Meaning of these Maven command line arguments is explained in
`rultor-queue` and `rultor-users` modules.

Create Auto Scaling group:

```
$ as-create-auto-scaling-group rultor-conveyer \
  --launch-configuration rultor-conveyer-0.1.10 \
  --availability-zones us-east-1d --min-size 1 --max-size 2 \
  --desired-capacity 1
```

AWS should launch the first EC2 instance automatically. That's it.

## How to re-configure existing Auto Scaling group

Create a new EC2 instance from existing AMI. Connect to the new EC2 instance
through SSH and make required changes.

Create new AMI from the running instance.

Create new AS group, using `as-create-launch-config` (as explained above).

Update existing AS group to use new launch config:

```
$ as-update-auto-scaling-group rultor-conveyer \
  --launch-configuration rultor-conveyer-0.1.11
```

Delete previous launch config:

```
$ as-delete-launch-config rultor-conveyer-0.1.10
```

De-register previous AMI.

Delete old EBS snapshot related to the previous AMI.


