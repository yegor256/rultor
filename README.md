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

There are three main operations you can perform with Rultor: merge, deploy and release. Every call to Rultor is posted in a Github issue as a comment (yes, any Github issue, in any Github project!)

Commands must start with `@rultor` followed by command text.

For example, in a pull request that is ready to be merged you post a new comment saying, literally:

``@rultor please check and merge this pull request``
It is important to start a command with `@rultor` and mention `merge` somewhere in the text. All the rest is ignored. You can achieve exactly the same thing by just posting:

``@rultor merge``

In order to find out more information please follow the following two links:

- [Rultor Commands](http://doc.rultor.com/commands.html)
- [Basic Commands](http://doc.rultor.com/basics.html)

<h6>More Useful Information</h6>

Rultor is not a replacement, but rather a powerful addition to your existing continuous integration solution, like [Jenkins](http://jenkins-ci.org/), [Go](https://www.thoughtworks.com/go/), [Travis](https://travis-ci.org/), [Drone](https://drone.io/), [Snap](https://snap-ci.com/), [Codeship](https://codeship.com/) or [Wercker](http://wercker.com/).

Rultor is absolutely free, both for open source and commercial projects. It is sponsored by [teamed.io](http://www.teamed.io/).

Here is how Rultor helps us to deploy/release automatically to [Maven Central](http://www.yegor256.com/2014/08/19/how-to-release-to-maven-central.html), [Rubygems](http://www.yegor256.com/2014/08/26/publish-to-rubygems.html), [Heroku](http://www.yegor256.com/2014/09/13/deploying-to-heroku.html), and [CloudBees](http://www.yegor256.com/2014/08/25/deploy-to-cloudbees.html).

Yeah, BTW, every build runs in its own [Docker](http://doc.rultor.com/docker.html) container!





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
