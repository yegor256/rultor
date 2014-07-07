/*globals casper:false */
[
  "/xml/index.xml",
  "/css/style.css",
  "/robots.txt",
  "/xsl/layout.xsl",
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
