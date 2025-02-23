/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

/*globals $:false, document:false, window:false */

$(document).ready(
  function () {
    "use strict";
    var $div = $('#pulse');
    window.setInterval(
      function () {
        $div.find('img').attr(
          'src',
          $div.attr('data-href') + '?' + Date.now()
        );
      },
      1000
    );
  }
);
