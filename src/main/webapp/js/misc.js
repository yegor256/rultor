/**
 * Copyright (c) 2009-2014, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*globals $: false, document: false, window: false */

window.qbaka || (function (a, c) {
  a.__qbaka_eh = a.onerror;
  a.__qbaka_reports = [];
  a.onerror = function () {
    a.__qbaka_reports.push(arguments);
    if (a.__qbaka_eh)try {
      a.__qbaka_eh.apply(a, arguments)
    } catch (b) {
    }
  };
  a.onerror.qbaka = 1;
  a.qbaka = {report: function () {
    a.__qbaka_reports.push([arguments, new Error()]);
  }, customParams: {}, set: function (a, b) {
    qbaka.customParams[a] = b
  }, exec: function (a) {
    try {
      a()
    } catch (b) {
      qbaka.reportException(b)
    }
  }, reportException: function () {
  }};
  var b = c.createElement("script"), e = c.getElementsByTagName("script")[0], d = function () {
    e.parentNode.insertBefore(b, e)
  };
  b.type = "text/javascript";
  b.async = !0;
  b.src = "//cdn.qbaka.net/reporting.js";
  "[object Opera]" == a.opera ? c.addEventListener("DOMContentLoaded", d) : d();
  qbaka.key = "e59595737b70f68465b6dc1971692095"
})(window, document);
qbaka.options = {autoStacktrace: 1, trackEvents: 1};

var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-1963507-28']);
_gaq.push(['_trackPageview']);
(function () {
    var ga = document.createElement('script');
    ga.type = 'text/javascript';
    ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0];
    s.parentNode.insertBefore(ga, s);
})();

var _prum = [
    ['id', '51fcbb82abe53dcf27000000'],
    ['mark', 'firstbyte', (new Date()).getTime()]
];
(function () {
    var s = document.getElementsByTagName('script')[0], p = document.createElement('script');
    p.async = 'async';
    p.src = '//rum-static.pingdom.net/prum.min.js';
    s.parentNode.insertBefore(p, s);
})();
