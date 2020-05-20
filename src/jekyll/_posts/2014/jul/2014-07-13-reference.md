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
file. The file is mandatory, but all content is optional.

This page contains a complete reference of YAML instructions in alphabetic order.

BTW, you can see a real-life configuration in
[jcabi](https://github.com/jcabi/jcabi) project:
[`rultor.yml`](https://github.com/jcabi/jcabi/blob/master/.rultor.yml).

## Architect

You may wish to define a role of an architect in your project,
who will supervise all merge/release/deploy commands. No command
of that kind will be executed without his confirmation:

{% highlight yaml %}
architect:
  - yegor256
{% endhighlight %}

This is enough to tell Rultor to ask for confirmation before running a build.

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
  secret.xml: "yegor256/secret-repo#assets/settings.xml"
{% endhighlight %}

This configuration tells Rultor that it needs to fetch `assets/settings.xml` from
`yegor256/secret-repo` and place it into the `secret.xml` file right before
starting a build.

Keep in mind that every builds starts in `/home/r/repo` directory,
while assets are placed one folder up in the directory tree,
in `/home/r`. This is how the directory layout looks:

{% highlight text %}
/home
  /r
    run.sh
    pid
    status
    stdout
    secret.xml
    /repo
      .rultor.yml
      pom.xml
      ...your other files...
{% endhighlight %}

Don't forget to add [@rultor](https://github.com/rultor) to the list of
collaborators in your private repository. Otherwise Rultor won't be
able to fetch anything from it.

The repository you're fetching assets from must contain `.rultor.yml`
where `friends` section should include the name of repository where
these assets are used, for example:

{% highlight yaml %}
friends:
  - yegor256/rultor
  - jcabi/*
{% endhighlight %}

You may also add `trustee` list there, in order to specify the list
of GitHub users who are allowed to modify the `.rultor.yml` file in the
repository, which is requesting the assets:

{% highlight yaml %}
trustees:
  - yegor256
{% endhighlight %}

This is a very useful and important security measure, which you have to
use when you have more than one contributor to the repository, in order
to make sure the instructions inside `.rultor.yml` don't do anything
illegal with your assets.

## Decrypt

You may want to keep your secret assets right inside your main
repository. In this case, in order to keep them secret, you should
encrypt them using [rultor remote](https://github.com/yegor256/rultor-remote):

{% highlight text %}
$ gem install rultor
$ rultor encrypt -p me/test secret.txt
{% endhighlight %}

Here `me/test` is the name of your Github project.

This code encrypts `secret.txt` file. You will get a new file `secret.txt.asc`.
Commit this file to your repository &mdash; nobody will be able
to read it, except Rultor server itself.

Then, instruct Rultor to decrypt it before running your build:

{% highlight yaml %}
decrypt:
  secret.txt: "repo/scrt/secret.txt.asc"
{% endhighlight %}

This configuration tells Rultor to get `scrt/secret.txt.asc` from the
root directory of your repository, decrypt it and save the result
into `secret.txt`. You can access it from your script as `../secret.txt`.
In other words, you can access it at `../secret.txt`, relative
to the repository root from where your script is executed.

## Docker Image

The default Docker image used for all commands is
[`yegor256/rultor`](https://registry.hub.docker.com/u/yegor256/rultor/).

You can change it to, say, `ubuntu:12.10`:

{% highlight yaml %}
docker:
  image: "ubuntu:12.10"
{% endhighlight %}

You can also use your own `Dockerfile` and build your own Docker image,
right before the build. Put `Dockerfile` in some directory in the repository
together with all other Docker files (if you need them) and provide a location
of that directory:

{% highlight yaml %}
docker:
  directory: repo/my-docker-image
{% endhighlight %}

## Environment Variables

You can specify environment variables common for all
commands, for example:

{% highlight yaml %}
env:
  MAVEN_OPTS: "-XX:MaxPermSize=256m -Xmx1g"
merge:
  script:
    - "mvn clean install"
deploy:
  script:
    - "mvn clean deploy"
{% endhighlight %}

In this example, `MAVEN_OPTS` environment variable will be set
for merging and deploying commands.

These environment variables will be available for you by default:

  * `author`: GitHub login of the user who sent the request to Rultor

## Install Script

You can specify script instructions common for all
commands, for example:

{% highlight yaml %}
install:
  - "sudo apt-get install texlive"
merge:
  script:
    - "latex ..."
deploy:
  script:
    - "latex ..."
{% endhighlight %}

In this example, `texlive` package will be installed before merge
and before deploy commands execution.

## Readers

By default, anyone can see your build logs. This may not be desired
for private projects. To grant access to your logs only for
a selected list of users, use this construct:

{% highlight yaml %}
readers:
  - "urn:github:526301"
  - "urn:github:8086956"
  - ...
{% endhighlight %}

Every user is specified as a URN, where his Github account number
stays right after `urn:github:`. You can get your Github account number
by logging into [www.rultor.com](https://www.rultor.com)
and moving mouse over your name, at the
top of the page.

## Run As Root

By default, we create a new user `r` in Docker container and
run your scripts from it. You can instruct Rultor to run
everything as root:

{% highlight yaml %}
docker:
  as_root: true
{% endhighlight %}

This may be a useful option when you are using a custom Docker
container with something different from Ubuntu inside. Switching
to a user `r` may not work smoothly under CentOS, for example. In
this case, just use root.

## SSH

By default, Rultor uses its own servers to run your builds. You can
change that by providing your own SSH coordinates:

{% highlight yaml %}
ssh:
  host: test.example.com
  port: 22
  key: ./keys/id_rsa
  login: test
{% endhighlight %}

Your servers must have `docker` installed. This is the only requirement.

## Merge, Deploy, Release

Three commands `merge`, `deploy` and `release` are
configured similarly in `.rultor.yml`. For example:

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

There are a few additional configurable parameters for `merge` section:

{% highlight yaml %}
merge:
  script: |
    echo "testing..."
    echo "building..."
    echo "packaging..."
  squash: true
  fast-forward: default
{% endhighlight %}

`squash` option may be set to `true` or `false` (default).

`fast-forward` may be either `default`
(`--ff` argument for Git), `only` (`--ff-only`) or `no` (`--no-ff`).
More information about it [here](http://git-scm.com/docs/git-merge).

`rebase` option may be set to `true` or `false` (default). If it's set
to `true`, your fork branch will be "rebased" from origin before the merge.

## Uninstall Script

When you need some script to be executed at the end of
every command (no matter what the result of that command is),
you can configure it via `uninstall`:

{% highlight yaml %}
install: |
  # create a new EC2 instance
merge:
  script: |
    # use EC2 instance for testing
uninstall: |
  # destroy EC2 instance
{% endhighlight %}

This mechanism can be useful when you want to free certain
resources, created during installation.
