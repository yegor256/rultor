---
layout: default
title: ".rultor.yml Reference"
date: 2014-07-13
description:
  Rultor is configured by the .rultor.yml,
  YAML configuration file in root directory of your Github repository
---

Rultor is configured solely through the YAML `.rultor.yml` file stored in the
root directory of your Github repository. There is no control or management
panel. Everything you want to say to Rultor is placed into your `.rultor.yml`
file.

This page contains a complete reference of commands in alphabetic order.

BTW, you can see a real-life configuration in [jcabi](https://github.com/jcabi/jcabi) project:
[`rultor.yml`](https://github.com/jcabi/jcabi/blob/master/.rultor.yml).

## Assets

Very often, you want to add secret files to the directory of your build, right
before it starts. For example, a file with database credentials that should be
deployed to production. You don't want to keep this file in the main repository
since it contains sensitive information, not intended to be accessible by all
programmers.

Put it into another *private* Github repository and inform Rultor that he has to
fetch it from there:

{% highlight yaml %}
assets:
  secret.xml: yegor256/secret-repo#assets/settings.xml
{% endhighlight %}

This configuration tells Rultor that needs to fetch `assets/settings.xml` from
`yegor256/secret-repo` and place it into the `secret.xml` file right before
starting a build.

Keep in mind that every builds starts in `repo` directory, while assets are
placed one folder up in the directory tree. This is how the directory layout
looks:

{% highlight text %}
.
..
run.sh
pid
status
stdout
secret.xml
repo/
  .rultor.yml
  pom.xml
  ...your other files...
{% endhighlight %}

Don't forget to add [@rultor](https://github.com/rultor) to the list of
collaborators in your private repository. Otherwise Rultor won't be
able to fetch anything from it.

## Docker Image

The default Docker image used for all commands is [`yegor256/rultor`](https://registry.hub.docker.com/u/yegor256/rultor/).

You can change it to, say, `ubuntu:12.10`:

{% highlight yaml %}
docker:
  image: ubuntu:12.10
{% endhighlight %}

## Install Script

You can specify script instructions common for all
commands, for example:

{% highlight yaml %}
install:
  - sudo apt-get install texlive
merge:
  script:
    - latex ...
deploy:
  script:
    - latex ...
{% endhighlight %}

In this example, `texlive` package will be installed before merge
and before deploy commands execution.
## Readers

By default, anyone can see your build logs. This may not be desired
for private projects. To grant access to your logs only for
a selected list of users, use this construct:

{% highlight yaml %}
readers:
  - urn:github:526301
  - urn:github:8086956
  - ...
{% endhighlight %}

Every user is specified as a URN, where his Github account number
stays right after `urn:github:`. You can get your Github account number
by logging into [www.rultor.com](http://www.rultor.com)
and moving mouse over your name, at the
top of the page.

## Merge, Deploy, Release

Three commands `merge`, `deploy` and `release` are configured similarly in `.rultor.yml`. For example:

{% highlight yaml %}
merge: # or "deploy" or "release"
  commanders:
    - jeff
    - walter
  env:
    MAVEN_OPTS: "-XX:MaxPermSize=256m -Xmx512m"
  script:
    - "sudo apt-get install graphviz"
    - "mvn clean install"
{% endhighlight %}

Environment variables have to be configured, as an associative array with names
of variables as keys, in the `env` property.

Executable script is configured as a list of texts. They will be executed one by
one. If any of them fails, execution stops.

The list of Github accounts able to give commands to Rultor is specified in
`commanders`. By default, only Github repository collaborators can give
commands. Configured commanders don't replace collaborators. In other words,
Github collaborators *and* accounts mentioned here are allowed to give commands.
