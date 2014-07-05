/*globals casper:false */
[
  "/xml/front.xml",
  "/xml/index.xml",
  "/xml/repos.xml",
  "/css/style.css",
  "/robots.txt",
  "/js/layout.js",
  "/xsl/layout.xsl",
  "/r"
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
