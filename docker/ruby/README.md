# Docker Image for Ruby Projects

Build it like this:

```bash
docker build --platform=linux/x86_64 -t yegor256/rultor-ruby .
```

Then, deploy it:

```bash
docker push yegor256/rultor-ruby
```

This image is used by [qbash](https://github.com/yegor256/qbash) and others.
