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
@reboot curl --silent https://raw.github.com/rultor/rultor/master/rultor-conveyer/src/main/resources/start.sh > start.sh && bash start.sh 2>&1 | logger -t rultor
```

Remove `/home/ubuntu/.ssh/authorized_keys` file.

Create AMI from this EC2 instance
([see how](http://docs.aws.amazon.com/AWSToolkitVS/latest/UserGuide/tkv-create-ami-from-instance.html))
and terminate the instance.

Install AS [command line tools](http://aws.amazon.com/developertools/2535).

Create new AS launch configuration ([more info](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/US_BasicSetup.html)):

```
$ as-create-launch-config rultor-conveyer-16 \
  --image-id ami-80a0d9e9 --instance-type t1.micro \
  --group rultor-conveyer --monitoring-disabled \
  --iam-instance-profile rultor-conveyer \
  --user-data '{"version":"url":"...","wallet_url":"","prefix":"...","pgsql_url":"...","pgsql_password":"..."}'
```

Create AS group:

```
$ as-create-auto-scaling-group rultor-conveyer \
  --launch-configuration rultor-conveyer-16 \
  --availability-zones us-east-1d --min-size 1 --max-size 2 \
  --tag "k=Name,v=rultor-conveyer,p=true" \
  --desired-capacity 1
```

AWS should launch the first EC2 instance automatically.

Now it's time to make this conveyer scalable on demand
(see [Auto Scaling with SQS](http://docs.aws.amazon.com/AutoScaling/latest/DeveloperGuide/as-using-sqs-queue.html)).
Create scaling-out and scaling-in policies:

```
$ as-put-scaling-policy rultor-sqs-scaleout-policy --auto-scaling-group rultor-conveyer --adjustment=1 --type ChangeInCapacity
$ as-put-scaling-policy rultor-sqs-scalein-policy --auto-scaling-group rultor-conveyer --adjustment=-1 --type ChangeInCapacity
```

Create CloudWatch alarms:

```
$ mon-put-metric-alarm --alarm-name AddCapacityToConveyer \
  --metric-name ApproximateNumberOfMessagesVisible \
  --namespace "AWS/SQS" --statistic Average --period 300 --threshold 500 \
  --comparison-operator GreaterThanOrEqualToThreshold --dimensions "QueueName=rultor" \
  --evaluation-periods 2 --alarm-actions [...ARN of the scale-out policy created above...]
$ mon-put-metric-alarm --alarm-name RemoveCapacityFromConveyer \
  --metric-name ApproximateNumberOfMessagesVisible \
  --namespace "AWS/SQS" --statistic Average --period 300 --threshold 50 \
  --comparison-operator LessThanOrEqualToThreshold  --dimensions "QueueName=rultor" \
  --evaluation-periods 2 --alarm-actions [...ARN of the scale-in policy created above...]
```

## How to re-configure existing AS group

Create a new EC2 instance from existing AMI. Connect to the new EC2 instance
through SSH and make required changes.

Create new AMI from the running instance.

Create new AS launch config, using `as-create-launch-config` (as explained above).

Update existing AS group to use new launch config:

```
$ as-update-auto-scaling-group rultor-conveyer \
  --launch-configuration rultor-conveyer-16
```

Delete previous launch config:

```
$ as-delete-launch-config rultor-conveyer-15
```

De-register previous AMI.

Delete old EBS snapshot related to the previous AMI.
