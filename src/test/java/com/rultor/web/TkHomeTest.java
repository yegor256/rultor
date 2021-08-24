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
package com.rultor.web;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.Toggles;
import com.rultor.spi.Talks;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rq.RqWithHeader;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkHome}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.50
 */
public final class TkHomeTest {

    /**
     * TkHome can render home page.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersHomePage() throws Exception {
        final Talks talks = new Talks.InDir();
        final Take take = new TkHome(talks, new Toggles.InFile());
        talks.create("repo1", "test1");
        talks.create("repo2", "test2");
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    take.act(
                        new RqWithHeader(new RqFake(), "Accept", "text/xml")
                    )
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                "/page/millis",
                "/page/recent[count(talk)=2]",
                "/page/links/link[@rel='ticks']",
                "/page/links/link[@rel='takes:github']",
                "/page/toggles/read-only"
            )
        );
    }

}
