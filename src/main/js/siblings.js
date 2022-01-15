/**
 * Copyright (c) 2009-2021 Yegor Bugayenko
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

document.addEventListener('DOMContentReady', () => {
  const DELTA = 600;

  const needMore = () => document.scrollingElement.scrollTop >=
    document.body.offsetHeight - window.visualViewport.height - DELTA;

  async function changeHandler () {
    const $box = document.getElementById('talks');
    const $tail = document.getElementById('tail');
    const more = $box.dataset.more;

    if (!more) {
      return true;
    }

    if (needMore()) {
      delete $box.dataset.more;
      $tail.style.display = 'block';

      const data = await fetch(more, {
        method: 'GET',
        cache: 'no-cache',
        headers: {
          'Accept': 'text/html',
          'Content-Type': 'text/xml',
        },
      })
        .then((res) => res.text())
        .catch((error) => {
          $tail.innerText = 'Oops, an error :( Please, try to reload the page';
        });

      if (data) {
        const parser = new DOMParser();
        const $fragment = parser.parseFromString(data, 'text/xml');
        const $newBox = fragment.getElementById('talks');
        const newEls = [...$newBox.children];
        for (let idx = 0; idx < newEls.length; idx += 1) {
          $box.appendChild(newEls[idx]);
        }
        $box.dataset.more = $newBox.dataset.more;
        $tail.style.display = 'none';
      }
    }
  }

  window.addEventListener('scroll', changeHandler);
  window.addEventListener('resize', changeHandler);
});
