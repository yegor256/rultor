// Copyright (c) 2009-2025 Yegor Bugayenko
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met: 1) Redistributions of source code must retain the above
// copyright notice, this list of conditions and the following
// disclaimer. 2) Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following
// disclaimer in the documentation and/or other materials provided
// with the distribution. 3) Neither the name of the rultor.com nor
// the names of its contributors may be used to endorse or promote
// products derived from this software without specific prior written
// permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
// NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
// THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

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
