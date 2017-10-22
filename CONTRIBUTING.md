## How to Contribute

Fork repository, make changes, send us a pull request. We will review
your changes and apply them to the `master` branch shortly, provided
they don't violate our quality standards. To avoid frustration, before
sending us your pull request please run full Maven build:

```
$ mvn clean install -Pqulice -PdockerITs
```

To avoid build errors use maven 3.3.x and have a Docker environment properly
loaded into the shell from which you run the build.
If your environment does not have the ability to run a working Docker client
and daemon, you can run the build without the Docker based integration tests
via:

```
$ mvn clean install -Pqulice
```

