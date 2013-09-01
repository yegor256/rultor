/**
 * Copyright (c) 2009-2013, rultor.com
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

function fetch($div) {
    var entry = $div.attr('data-fetch-url');
    if (!entry) {
        console.log('fetch URL is absent!');
        return;
    }
    if ($div.attr('data-fetch-stop')) {
        return;
    }
    $div.find('.heart').addClass('text-warning');
    $div.find('.snapshot').load(
        entry,
        function(text, status, xhr) {
            if (status == "error") {
                $div.find('.heart').addClass('text-danger');
                $div.find('.heart').attr('title', text);
            } else {
                $div.find('.heart').removeClass('text-warning');
                RULTOR.format($div);
                setTimeout(function() { fetch($div); }, 5000);
            }
        }
    );
}

$(document).ready(
    function() {
        $('div:has(.snapshot)').each(
            function () {
                fetch($(this));
            }
        );
        $('.heart').each(
            function () {
                $(this).click(
                    function() {
                        var $div = $(this).parent().parent().parent();
                        if ($div.attr('data-fetch-stop')) {
                            $div.removeAttr('data-fetch-stop');
                            $(this).removeClass('text-danger');
                            fetch($div);
                        } else {
                            $div.attr('data-fetch-stop', 'yes');
                            $(this).addClass('text-danger');
                        }
                    }
                );
            }
        );
    }
);
