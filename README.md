<img src="http://doc.rultor.com/images/logo.svg" width="64px" height="64px"/>

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/rultor)](http://www.rultor.com/p/yegor256/rultor)

[![Coverage Status](https://coveralls.io/repos/yegor256/rultor/badge.svg?branch=__rultor&service=github)](https://coveralls.io/github/yegor256/rultor?branch=__rultor)
[![Dependencies](https://www.versioneye.com/user/projects/561a9d87a193340f28000fd3/badge.svg?style=flat)](https://www.versioneye.com/user/projects/561a9d87a193340f28000fd3)

[![Issue Stats](http://issuestats.com/github/yegor256/rultor/badge/issue)](http://issuestats.com/github/yegor256/rultor)
[![Issue Stats](http://issuestats.com/github/yegor256/rultor/badge/pr)](http://issuestats.com/github/yegor256/rultor)

Full documentation is at [doc.rultor.com](http://doc.rultor.com)

These blog posts may be helpful:
[Every Build in Its Own Docker Container](http://www.yegor256.com/2014/07/29/docker-in-rultor.html),
[Master Branch Must Be Read-Only](http://www.yegor256.com/2014/07/21/read-only-master-branch.html),
[Rultor + Travis](http://www.yegor256.com/2014/07/31/travis-and-rultor.html), and
[Rultor, a Merging Bot](http://www.yegor256.com/2014/07/24/rultor-automated-merging.html).

Default Docker image is [yegor256/rultor](https://registry.hub.docker.com/u/yegor256/rultor/)

## What Is Rultor?

Rultor is a DevOps team assistant. It helps your programmers and release managers automate routine operations, with an easy-to-use intuitive interface:

- Merging of pull requests (ensures that builds remain clean)
- Deploying to production and stage environments
- Releasing and tagging

There are three main operations you can perform with Rultor: merge, deploy and release. Every call to Rultor is posted in a Github issue as a comment (yes, any Github issue, in any Github project!).

Commands must start with `@rultor` followed by command text.

For example, in a pull request that is ready to be merged you post a new comment saying, literally:

```
@rultor please check and merge this pull request
```


It is important to start a command with ```@rultor``` and mention `merge` somewhere in the text. All the rest is ignored. You can achieve exactly the same thing by just posting:

```
@rultor merge
```

<h3>Basic Commands</h3>
<h5>Merge</h5>

It is a very good practice, to run your automated build after merging a pull request into the master branch and right before pushing changes. When your pull request is ready, ask Rultor to merge it. Rultor will checkout your master branch, merge changes into it, run the automated build, and push the new version of master to your repo.

Don't forget to create [rultor.yml](http://doc.rultor.com/reference.html) in the root directory of your repository and configure the build automation script there. For example,


```
merge:
  script: mvn clean install
```


If you don't configure it, Rultor will try to guess your build automation tool and call it accordingly. If you have ``pom.xml`` in your root directory — it is [Maven](http://maven.apache.org/); if you have ``Gemfile`` — it is [Bundler](http://bundler.io/).

We strongly recommend that you configure your script in [``rultor.yml``](http://doc.rultor.com/reference.html).

<h5>Deploy</h5>

All you need to do is define your deployment scenario as a bash script and configure it in [``.rultor.yml``](http://doc.rultor.com/reference.html).

For example, this is how we deploy Rultor itself (see our [``.rultor.yml``](https://github.com/yegor256/rultor/blob/master/.rultor.yml) file):


```
deploy:
  env:
    MAVEN_OPTS: "-XX:MaxPermSize=256m -Xmx1g"
  script:
    - "sudo bundle"
    - "mvn clean deploy -Prultor --settings ../settings.xml"
    - "mvn clean site-deploy -Psite -Prempl --settings ../settings.xml"
```

First, we install all Ruby gems required for Jekyll (this is how this website is built). Then, we deploy the system to CloudBees. Then, we deploy the site to Github Pages.

<h5>Release</h5>

Release is when you package a stable version of your product and make it available for everybody in an artifact repository. For example, [Maven Central](http://search.maven.org/) or [RubyGems](https://rubygems.org/).

The Release command automates this for you. The command does require one mandatory argument, though. You should call it like this:

```
@rultor release, tag=`1.7`
```

Then, in your script you get ``$tag`` environment variable, which will be set to ``1.7``. Your script should change the version of the product to 1.7 and build it.

This is how we do it in jcabi [rultor.yml.](https://github.com/jcabi/jcabi/blob/master/.rultor.yml) For example:

```
release:
  script: |
    mvn versions:set "-DnewVersion=$tag"
    git commit -am "$tag"
    mvn clean deploy -Pqulice -Psonatype -Pjcabi
    mvn clean site-deploy -Psite -Prempl --settings ../settings.xml
```

We change the version of the product to the one provided in ``$tag``, then commit the changes, build and deploy to Sonatype. Next, we build the project site and deploy it to Github Pages. All the rest is done by Rultor. Basically, this is what will be done:

1. Check out your repository
2. Run the script configured in .rultor.yml
3. Tag the latest commit as $tag
4. Push the new tag to the repository

Of course, if any of these steps fails, the entire command fails and you get a full log in the Github issue.

<h3>Rultor Commands</h3>

All commands you give to Rultor should start with ``@rultor`` and be followed by a text, which includes a mnemo of a command. For example, in order to check the status of Rultor in current conversation (ticket), you can post a comment:

```
@rultor what is the current status?
```

The mnemo of the command here is ``status``. All the rest is ignored. You can achieve exactly the same by just posting:

```
@rultor status
```

BTW, all commands are configured through [rultor.yml](http://doc.rultor.com/reference.html).

<h5>Config</h5>
This command will show you how Rultor understands your ``.rultor.yml`` file. Rultor translates YAML into XML, for the sake of simplicity and strictness.

<h5>Deploy</h5>

Deploying packaged product to production (or test) environment, in order to make it available for end-users (or testers). More details are in above mentioned Basic Commands.

<h5>Hello</h5>

Sort of a "ping". Post ``@rultor hello`` and expect an immediate answer from Rultor (well, within 60 seconds). If you don't get an answer, there is something wrong with Rultor.

<h5>Lock and Unlock</h5>

You can "lock" any branch to prevent merges there. Sometimes this may be a very valuable feature, when, say, you want to make sure that for a short period of time only you will merge something into some branch, to stabilize it. When it's stable, you release a new version and unlock the branch. To lock it, say ``@rultor lock``. That's it.

By default, branch ``master`` will be locked. In order to lock a branch, Rultor creates a simple text file ``.rultor.lock`` in the branch. The file contains a list of Github user names, who are allowed to merge into the branch.

If you want to change the list of users or lock some other branch, post ``@rultor lock branch=`master`, users=`jeff,yegor256```.

To unlock, post ``@rultor unlock branch=`master. Again, the default branch ismaster```.

<h5>Merge</h5>

Merges pull request, checking its validity beforehand. More details are in above mentioned Basic Commands.

<h5>Release</h5>

Releases a stable version of the product to artifact repository, like Maven Central of RubyGems. More details are here. More details are in above mentioned Basic Commands.

<h5>Status</h5>

Checks the status of currently running command in current conversation. When a command takes long this command may be convenient to use. Just post ``@rultor status`` and see what Rultor says.

<h5>Stop</h5>

Rultor will try to immediately stop/kill current command. Just post ``@rultor stop``.

<h5>Version</h5>

This command shows up current version of Rultor engine.


## What Problem Does Rultor Solve?

TBD... _stakeholders and needs_

## How Rultor Works?

TBD... _actors and features_

## What Is Rultor SLA?

TBD... _quality requrirements_

## How to Contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice
```

To avoid build errors use maven 3.2+

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/yegor256/rultor/issues/new).

## Continuous Integration

We're using all possible continuous integration systems, mostly
for fun :) Here is our [selection criteria](http://www.yegor256.com/2014/10/05/ten-hosted-continuous-integration-services.html).

[travis-ci.org](http://www.travis-ci.org)<br/>
[![Build Status](https://travis-ci.org/yegor256/rultor.svg?branch=master)](https://travis-ci.org/yegor256/rultor)

[appveyor.com](http://www.appveyor.com)<br/>
[![Build status](https://ci.appveyor.com/api/projects/status/sulqrjerl27qqtl7/branch/master?svg=true)](https://ci.appveyor.com/project/yegor256/rultor/branch/master)

[codeship.io](http://www.codeship.io)<br/>
[![Codeship Status for yegor256/rultor](https://codeship.io/projects/d00b5ff0-2641-0132-d783-12f2cec1461b/status?branch=master)](https://codeship.io/projects/37414)

[wercker.com](http://www.wercker.com)<br/>
[![wercker status](https://app.wercker.com/status/0e6506c69e078b7692e50b240c034524/s "wercker status")](https://app.wercker.com/project/bykey/0e6506c69e078b7692e50b240c034524)

[circleci.io](http://www.circleci.io)<br/>
[![Circle CI](https://circleci.com/gh/yegor256/rultor.png?style=badge)](https://circleci.com/gh/yegor256/rultor)

[magnum-ci.com](http://www.magnum-ci.com)<br/>
[![Build Status](https://magnum-ci.com/status/ebf25febbbf66f3c3cd411c94a4ed3d4.png)](https://magnum-ci.com/public/0ab38d64b0ab19293711/builds)

[shippable.com](http://www.shippable.com)<br/>
[![Build Status](https://api.shippable.com/projects/542e8fb980088cee586d3806/badge?branchName=master)](https://app.shippable.com/projects/542e8fb980088cee586d3806/builds/latest)

Less interesting and/or stable stuff:

[snap-ci.com](http://www.snap-ci.com)<br/>
[![Build Status](https://snap-ci.com/yegor256/rultor/branch/master/build_image)](https://snap-ci.com/yegor256/rultor/branch/master)

[semaphoreapp.com](http://www.semaphoreapp.com)<br/>
[![Build Status](https://semaphoreapp.com/api/v1/projects/115d317a-9f15-4c71-9301-5dae64f0a76d/260906/badge.png)](https://semaphoreapp.com/yegor256/rultor)

[drone.io](http://www.drone.io)<br/>
[![Build Status](https://drone.io/github.com/yegor256/rultor/status.png)](https://drone.io/github.com/yegor256/rultor/latest)

[solanolabs.com](http://ci.solanolabs.com)<br/>
[![sonolabs status](https://ci.solanolabs.com:443/yegor256/rultor/badges/120059.png?badge_token=6c00577e47c05198703fe752d6d26cd4e4a4d011)](https://ci.solanolabs.com:443/yegor256/rultor/suites/120059)
