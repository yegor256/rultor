# The file is not complete

RUN apt-get install -y bcrypt

# PHP installation
sudo apt-get install php5-dev
sudo apt-get install php-pear

# jsl
wget http://www.javascriptlint.com/download/jsl-0.3.0-src.tar.gz
tar xzf jsl-0.3.0-src.tar.gz
cd jsl-0.3.0/src
make -f Makefile.ref
mv Linux_All_DBG.OBJ/jsl /usr/local/bin

# xdebug
pecl install xdebug
echo "zend_extension=xdebug.so" > /etc/php5/cli/conf.d/xdebug.ini

sudo apt-get install jq
