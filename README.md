<img src="http://doc.rultor.com/images/logo.svg" width="64px" height="64px"/>

[![Managed by Zerocracy](http://www.0crat.com/badge/C3SAYRPH9.svg)](http://www.0crat.com/p/C3SAYRPH9)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/rultor)](http://www.rultor.com/p/yegor256/rultor)
[![We recommend IntelliJ IDEA](http://img.teamed.io/intellij-idea-recommend.svg)](https://www.jetbrains.com/idea/)

[![Availability at SixNines](http://www.sixnines.io/b/efd7)](http://www.sixnines.io/h/efd7)
[![Coverage Status](https://coveralls.io/repos/yegor256/rultor/badge.svg?branch=__rultor&service=github)](https://coveralls.io/github/yegor256/rultor?branch=__rultor)
[![PDD status](http://www.0pdd.com/svg?name=yegor256/rultor)](http://www.0pdd.com/p?name=yegor256/rultor)
[![codebeat badge](https://codebeat.co/badges/56116205-91d3-4966-8f15-d5c505fc3905)](https://codebeat.co/projects/github-com-yegor256-rultor)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3d1858b2edfc4dcdae9363c09a75dfbc)](https://www.codacy.com/app/github_90/rultor?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yegor256/rultor&amp;utm_campaign=Badge_Grade)
[![Dependencies](https://www.versioneye.com/user/projects/561a9d87a193340f28000fd3/badge.svg?style=flat)](https://www.versioneye.com/user/projects/561a9d87a193340f28000fd3)
[![Join the chat at https://gitter.im/yegor256/rultor](https://badges.gitter.im/yegor256/rultor.svg)](https://gitter.im/yegor256/rultor?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Conference recommends](https://devternity.com/shields/recommends.svg)](https://devternity.com)

Full documentation is at [doc.rultor.com](http://doc.rultor.com)

These blog posts may be helpful:
[Every Build in Its Own Docker Container](http://www.yegor256.com/2014/07/29/docker-in-rultor.html),
[Master Branch Must Be Read-Only](http://www.yegor256.com/2014/07/21/read-only-master-branch.html),
[Rultor + Travis](http://www.yegor256.com/2014/07/31/travis-and-rultor.html), and
[Rultor, a Merging Bot](http://www.yegor256.com/2014/07/24/rultor-automated-merging.html).

Default Docker image is [yegor256/rultor](https://registry.hub.docker.com/u/yegor256/rultor/)

## What Is Rultor?

Rultor is a DevOps team assistant. It helps your programmers and release managers automate routine operations (merge, deploy and release), with an easy-to-use intuitive interface.

## What Problems Does Rultor Solve?

Automated deployment scripts have been around for some time. Rultor attempts to
tackle the problems those scripts do not.

The first benefit of Rultor is that it gives you isolation of your deployment
script in its own virtual environment by using Docker containers. This
substantially reduces the amount of external state that could affect your build
and makes errors more easily reproducible.

Additionally, because of the way Rultor integrates with modern issue trackers,
all the logs are stored and published to the ticket on which Rultor was
mentioned. Making vital information easily accessible to all developers.

Rultor performs pre-flight builds. Instead of merging into master and then
seeing if your changes broke the build or not, Rultor checks out the master
branch, apply your changes to it, then runs everything it was set up to run.
If, and only if, everything goes well, Rultor merges the changes into master.
This programmatically prevents the master from being broken by developers. Not
having to worry about breaking the build for everyone else has a very positive
impact in the way developers write code, increasing their productivity and
mitigating their fear of making mistakes.

Lastly, Rultor provides an integrated and humanized interface to DevOps tools,
as a human-readable sentence suffices to trigger a merge or a release.

## How Rultor Works?
Once Rultor finds a [merge command](http://doc.rultor.com/basics.html)
in one of your GitHub pull requests, it does exactly this:

1. Reads the [.rultor.yml](http://doc.rultor.com/reference.html)
 YAML configuration file from the root directory of your repository.
2. Gets automated build execution command from it, for example `bundle test`.
3. Checks out your repository into a temporary directory on one of its servers.
4. Merges pull request into `master` branch.
5. Starts a new Docker container and runs the build execution command in it, for example `bundle test`.
6. If everything is fine, pushes modified `master` branch to GitHub.
7. Reports back to you, in the GitHub pull request.

You can see it in action, for example, in this pull request:
[jcabi/jcabi-github#878](https://github.com/jcabi/jcabi-github/pull/878).

## What Is Rultor SLA?

TBD... _quality requirements_

## How to Contribute

If you are interested in contributing please refer to [CONTRIBUTING.md](CONTRIBUTING.md)


## Docker server

The server where Docker works must be Ubuntu 16.04.

[Install](https://docs.docker.com/engine/installation/linux/ubuntu/) Docker.

Create `rultor` user and add it to `docker` group.

Add `rultor` to sudoers.

```
apt-get update -y
apt-get install -y mailx
apt-get install -y gnupg2
apt-get install -y bc
```

## Got questions?

If you have questions or general suggestions, don't hesitate to submit
a new [Github issue](https://github.com/yegor256/rultor/issues/new).

## Continuous Integration

We're using all possible continuous integration systems, mostly
for fun :) Here is our [selection criteria](http://www.yegor256.com/2014/10/05/ten-hosted-continuous-integration-services.html).

[travis-ci.org](http://www.travis-ci.org)<br/>
[![Build Status](https://travis-ci.org/yegor256/rultor.svg?branch=master)](https://travis-ci.org/yegor256/rultor)

[shippable.com](http://www.shippable.com)<br/>
[![Build Status](https://api.shippable.com/projects/542e8fb980088cee586d3806/badge?branchName=master)](https://app.shippable.com/projects/542e8fb980088cee586d3806/builds/latest)

[appveyor.com](http://www.appveyor.com)<br/>
[![Build status](https://ci.appveyor.com/api/projects/status/sulqrjerl27qqtl7/branch/master?svg=true)](https://ci.appveyor.com/project/yegor256/rultor/branch/master)

[wercker.com](http://www.wercker.com)<br/>
[![wercker status](https://app.wercker.com/status/0e6506c69e078b7692e50b240c034524/s "wercker status")](https://app.wercker.com/project/bykey/0e6506c69e078b7692e50b240c034524)

Less interesting and/or stable stuff:

[codeship.io](http://www.codeship.io)<br/>
[![Codeship Status for yegor256/rultor](https://codeship.io/projects/d00b5ff0-2641-0132-d783-12f2cec1461b/status?branch=master)](https://codeship.io/projects/37414)

[circleci.io](http://www.circleci.io)<br/>
[![Circle CI](https://circleci.com/gh/yegor256/rultor.png?style=badge)](https://circleci.com/gh/yegor256/rultor)

[semaphoreapp.com](http://www.semaphoreapp.com)<br/>
[![Build Status](https://semaphoreapp.com/api/v1/projects/115d317a-9f15-4c71-9301-5dae64f0a76d/260906/badge.png)](https://semaphoreapp.com/yegor256/rultor)

[solanolabs.com](http://ci.solanolabs.com)<br/>
[![sonolabs status](https://ci.solanolabs.com:443/yegor256/rultor/badges/120059.png?badge_token=6c00577e47c05198703fe752d6d26cd4e4a4d011)](https://ci.solanolabs.com:443/yegor256/rultor/suites/120059)
