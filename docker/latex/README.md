# Docker Image for LaTeX Projects

Build it like this:

```bash
docker build --platform=linux/x86_64 -t yegor256/rultor-latex .
```

Then, deploy it:

```bash
docker push yegor256/rultor-latex
```

This image is used by [takes](https://github.com/yegor256/ffcode) and others.
