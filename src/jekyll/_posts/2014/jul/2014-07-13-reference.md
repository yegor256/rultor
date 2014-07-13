---
layout: default
title: ".rultor.yml Reference"
date: 2014-07-13
description:
  Rultor is configured by .rultor.yml, YAML configuration
  file in root directory of your Github repository
---

Rultor is configures solely through YAML `.rultor.yml` file
stored in the root directory of your Github repository. This page
contains a complete reference of it (in alphabetic order).

BTW, a real-life configuration you can see in [jcabi](https://github.com/jcabi/jcabi) project:
[`rultor.yml`](https://github.com/jcabi/jcabi/blob/master/.rultor.yml).

## Assets

Very often you want to add some secret files to the directory of
your build, right before it starts. For example, a file with database
credentials, that should be deployed to production. You don't want
to keep this file in the main repository, since it contains sensitive
information not desirable to be accessed by all programmers.

Put it into another private Github repository and inform Rultor that
it has to fetch them from there:

{% highlight yaml %}
assets:
  secret.xml: yegor256/secret-repo#assets/settings.xml
{% endhighlight %}

This configuration tells Rultor that is has to fetch `assets/settings.xml`
from `yegor256/secret-repo` and place it into `secret.xml` file
right before starting a build.

Keep in mind that every builds starts in `repo` directory, while
assets are placed one folder up. This is how it will looks:

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

Don't forget to add [@rultor](https://github.com/rultor) to the
list of collaborators in your private repository. Otherwise he won't
be able to fetch anything from it.

## Docker Image

Default Docker image used for all commands is
[`yegor256/rultor`](https://registry.hub.docker.com/u/yegor256/rultor/).
You can change it to, say, `ubuntu:12.10`:

{% highlight yaml %}
docker:
  image: ubuntu:12.10
{% endhighlight %}

## Merge, Deploy, Release

Three commands `merge`, `deploy` and `release` are configured similarly in
`.rultor.yml`, for example:

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

Environment variables have to be configured as an associative array
with names of variables as keys, in `env` property.

Executable script is configured as a list of texts. They will be
executed one by one. If any of them fails, execution will stop.

List of Github accounts who can give commands to Rultor is specified
in `commanders`. By default, only Github repository collaborators
can give commands. Commanders configured don't replace collaborators. In other
words, Github collaborators *and* accounts mentioned here are allowed
to give commands.
