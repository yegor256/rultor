<img src="http://doc.rultor.com/images/logo.svg" width="64px" height="64px"/>

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/yegor256/rultor)](http://www.rultor.com/p/yegor256/rultor)
[![We recommend IntelliJ IDEA](http://img.teamed.io/intellij-idea-recommend.svg)](https://www.jetbrains.com/idea/)

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

TBD... _product statement_

## What Problem Does Rultor Solve?

TBD... _stakeholders and needs_

## How Rultor Works?
Once Rultor finds a <a href="http://doc.rultor.com/basics.html">merge command</a> 
in one of your GitHub pull requests, it does exactly this:
<ol>
<li>Reads the <a href="http://doc.rultor.com/reference.html"><code>.rultor.yml</code>
</a> YAML configuration file from the root directory of your repository.</li>
<li>Gets automated build execution command from it, for example <code>bundle test</code>.</li>
<li>Checks out your repository into a temporary directory on one of its servers.</li>
<li>Merges pull request into <code>master</code> branch.</li>
<li>Starts a new Docker container and runs <code>bundle test</code> in it.</li>
<li>If everything is fine, pushes modified <code>master</code> branch to GitHub.</li>
<li>Reports back to you, in the GitHub pull request.</li>
</ol>
You can see it in action, for example, in this pull request: 
<a href="https://github.com/jcabi/jcabi-github/pull/878">jcabi/jcabi-github#878</a>.

## What Is Rultor SLA?

TBD... _quality requirements_

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
