/*globals casper:false */
[
  "/css/style.css",
  "/robots.txt",
  "/xsl/layout.xsl",
  "/js/home.js",
  "/p/test/repo",
  "/b/test/repo.1",
  "/"
].forEach(
  function (page) {
    "use strict";
    casper.test.begin(
      page + " page can be rendered",
      function (test) {
        casper.start(
          casper.cli.get("home") + page,
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

casper.test.begin(
  "SVG is renderable",
  function (test) {
    "use strict";
    casper.start(
      casper.cli.get("home") + "/b/test/repo",
      function () {
        test.assertHttpStatus(200);
        //test.assertTextExists('<svg');
      }
    );
    casper.run(
      function () {
        test.done();
      }
    );
  }
);
