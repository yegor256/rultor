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
        // Check whether the #pulse div
        // and the img tag does exists
        if ($div.length && $div.find('img').length) {
          $div.find('img').attr(
            'src',
            $div.attr('data-href') + '?' + Date.now()
          );
        }
      },
      1000
    );
  }
);
