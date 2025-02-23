/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

/*globals $:false, document:false, window:false */

$(document).ready(
  function () {
    "use strict";
    $(window).scroll(
      function () {
        var $box = $('#talks'), more = $box.attr('data-more'), $tail = $('#tail');
        if ($(window).scrollTop() >= $(document).height() - $(window).height() - 600 && more) {
          $box.removeAttr('data-more');
          $tail.show();
          $.ajax(
            {
              url: more,
              cache: false,
              dataType: 'xml',
              headers: { 'Accept': 'text/html' },
              method: 'GET',
              success: function (data) {
                var $div = $(data).find('#talks');
                $box.html($box.html() + $div.html());
                $box.attr('data-more', $div.attr('data-more'));
                $tail.hide();
              },
              error: function () {
                $tail.html('Oops, an error :( Please, try to reload the page');
              }
            }
          );
        }
      }
    );
  }
);
