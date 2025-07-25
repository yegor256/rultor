# DevOps Assistant in GitHub

![logo](https://doc.rultor.com/images/logo.svg)

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](https://www.rultor.com/b/yegor256/rultor)](https://www.rultor.com/p/yegor256/rultor)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/yegor256/rultor/actions/workflows/mvn.yml/badge.svg)](https://github.com/yegor256/rultor/actions/workflows/mvn.yml)
[![codecov](https://codecov.io/gh/yegor256/rultor/branch/master/graph/badge.svg)](https://codecov.io/gh/yegor256/rultor)
[![PDD status](https://www.0pdd.com/svg?name=yegor256/rultor)](https://www.0pdd.com/p?name=yegor256/rultor)
[![codebeat badge](https://codebeat.co/badges/56116205-91d3-4966-8f15-d5c505fc3905)](https://codebeat.co/projects/github-com-yegor256-rultor)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8d048ba877e34b168d50f13b1c079824)](https://www.codacy.com/gh/yegor256/rultor/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=yegor256/rultor&amp;utm_campaign=Badge_Grade)
[![Hits-of-Code](https://hitsofcode.com/github/yegor256/rultor)](https://hitsofcode.com/view/github/yegor256/rultor)
[![Availability at SixNines](https://www.sixnines.io/b/efd7)](https://www.sixnines.io/h/efd7)

[Rultor](https://www.rultor.com) is a DevOps team assistant.
It helps your programmers and release managers automate
  routine operations (merge, deploy, and release)
  with an easy-to-use intuitive chat-bot interface.
Just say `@rultor hello` in any of your GitHub issues and
  the conversation will start.

Full documentation is at [doc.rultor.com](https://doc.rultor.com)

[Schema](rultor_schema.json) for .rultor.yml file.

Need help online? Try our [Telegram group](https://t.me/zerocracy).

These blog posts may be helpful:

* [_Rultor, a Merging Bot_][1]
* [_Every Build in Its Own Docker Container_][2]
* [_Master Branch Must Be Read-Only_][3]
* [_Rultor + Travis_][4]

Watch these videos to understand what Rultor is for:

* [_Deployment Scripts Are Dead; Meet Rultor_][5]:
DevOps Pro; Vilnius, Lithuania; 26 May 2016

* [_A Practical Example of a One-Click Release_][6]:
DevOpsPro 2016; Moscow, Russia; 15 November 2016

* [_Chat Bots Architecture_][7]:
GeekOUT 2016; Tallinn, Estonia; 9 June 2016

Default Docker image is
[yegor256/rultor-image](https://hub.docker.com/r/yegor256/rultor-image/).

## What Problems Does Rultor Solve?

Automated deployment scripts have been around for some time.
Rultor attempts to tackle the problems those scripts do not.

The first benefit of Rultor is that it gives you isolation of your deployment
  script in its own virtual environment by using Docker containers.
This substantially reduces the amount of external state that could
  affect your build and makes errors more easily reproducible.

Additionally, because of the way Rultor integrates with modern issue trackers,
  all the logs are stored and published to the ticket on which Rultor was
  mentioned, making vital information easily accessible to all developers.

Rultor performs pre-flight builds. Instead of merging into master and then
seeing if your changes broke the build or not, Rultor checks out the master
branch, applies your changes to it, then runs everything it was set up to run.
If, and only if, everything goes well, Rultor merges the changes into master.
This programmatically prevents the master from being broken by developers. Not
having to worry about breaking the build for everyone else has a very positive
impact on the way developers write code, increasing their productivity and
mitigating their fear of making mistakes.

Lastly, Rultor provides an integrated and humanized interface to DevOps tools,
as a human-readable sentence suffices to trigger a merge or a release.

## How Does Rultor Work?

Once Rultor finds a [merge command](https://doc.rultor.com/basics.html)
in one of your GitHub pull requests, it does exactly this:

1. Reads the [.rultor.yml](https://doc.rultor.com/reference.html)
YAML configuration file from the root directory of your repository.
1. Gets automated build execution command from it, for example `bundle test`.
1. Checks out your repository into a temporary directory on one of its servers.
1. Merges pull request into `master` branch.
1. Starts a new Docker container and runs the build execution command,
for example `bundle test`.
1. If everything is fine, pushes modified `master` branch to GitHub.
1. Reports back to you, in the GitHub pull request.

You can see it in action, for example, in this pull request:
[jcabi/jcabi-github#878](https://github.com/jcabi/jcabi-github/pull/878).

[1]: http://www.yegor256.com/2014/07/24/rultor-automated-merging.html
[2]: http://www.yegor256.com/2014/07/29/docker-in-rultor.html
[3]: http://www.yegor256.com/2014/07/21/read-only-master-branch.html
[4]: http://www.yegor256.com/2014/07/31/travis-and-rultor.html
[5]: https://www.youtube.com/watch?v=NflR7DKwxDY
[6]: https://www.youtube.com/watch?v=_61CuGhyv-o
[7]: https://www.youtube.com/watch?v=7yTIWFZrXpg
