/*globals casper:false */
[
    '/xml/front.xml',
    '/xml/index.xml',
    '/xml/rule.xml',
    '/css/style.css',
    '/robots.txt',
    '/js/stand.js',
    '/'
].forEach(
    function (page) {
        casper.test.begin(
            page + ' page can be rendered',
            function (test) {
                casper.start(
                    casper.cli.get('home') + page,
                    function () {
                        test.assertHttpStatus(200);
                    }
                );
                casper.run(
                    function () {
                        test.done();
                    }
                );
            }
        );
    }
);
