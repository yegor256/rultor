FROM ubuntu:14.04

RUN apt-get update
RUN apt-get install -y ruby
RUN apt-get install -y bcrypt

# PHP installation
RUN apt-get install -y php5-dev php-pear

# jsl
RUN cd /tmp && \
  mkdir jsl && \
  wget http://www.javascriptlint.com/download/jsl-0.3.0-src.tar.gz && \
  tar xzf jsl-0.3.0-src.tar.gz && \
  cd jsl-0.3.0/src && \
  make -f Makefile.ref && \
  mv Linux_All_DBG.OBJ/jsl /usr/local/bin && \
  cd /tmp && \
  rm -rf jsl

# xdebug
RUN pecl install xdebug && \
  echo "zend_extension=xdebug.so" > /etc/php5/cli/conf.d/xdebug.ini

RUN apt-get install -y jq
RUN apt-get install -y gnupg gnupg2

