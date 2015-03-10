# The file is not complete

RUN apt-get install -y bcrypt

# PHP installation
sudo apt-get install php-pear
pear channel-discover pear.phing.info
pear install --alldeps phing/phing
sudo apt-get install php5-dev

# jsl
wget http://www.javascriptlint.com/download/jsl-0.3.0-src.tar.gz
tar xzf jsl-0.3.0-src.tar.gz
cd jsl-0.3.0/src
make -f Makefile.ref
mv Linux_All_DBG.OBJ/jsl /usr/local/bin

# phpmd
pear channel-discover pear.pdepend.org
pear install pdepend/PHP_Depend
pear channel-discover pear.phpmd.org
pear install phpmd/PHP_PMD

# xdebug
pecl install xdebug
echo "zend_extension=xdebug.so" > /etc/php5/cli/conf.d/xdebug.ini
