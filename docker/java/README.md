# Docker Image for Java Projects

Build it like this:

```bash
docker build --platform=linux/x86_64 -t yegor256/rultor-java .
```

Then, deploy it:

```bash
docker push yegor256/rultor-java
```

This image is used by [qbash](https://github.com/yegor256/qbash) and others.
