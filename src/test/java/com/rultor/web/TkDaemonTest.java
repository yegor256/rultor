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
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.auth.PsFake;
import org.takes.facets.auth.TkAuth;
import org.takes.facets.fork.RqRegex;
import org.takes.rq.RqFake;
import org.xembly.Directives;

/**
 * Test case for {@link TkDaemon}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.50
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkDaemonTest {

    /**
     * TkDaemon can show log in HTML.
     * @throws Exception If some problem inside
     */
    @Test
    public void showsLogInHtml() throws Exception {
        final Talks talks = new Talks.InDir();
        final String name = "test";
        talks.create(name, Talk.TEST_NAME);
        final Talk talk = talks.get(name);
        final File tail = File.createTempFile(
            TkDaemonTest.class.getCanonicalName(), ".txt"
        );
        final String content = "1 < привет > тебе от меня";
        FileUtils.writeStringToFile(tail, content);
        talk.modify(
            new Directives().xpath("/talk").add("daemon")
                .attr("id", "00000000")
                .add("dir").set(tail.getAbsolutePath()).up()
                .add("script").set("no script").up()
                .add("title").set("no title")
        );
        final Take take = new TkAuth(
            new Take() {
                @Override
                public Response act(final Request request) throws IOException {
                    return new TkDaemon(talks).act(
                        new RqRegex.Fake("(.*)-(.*)", "1-abcd")
                    );
                }
            },
            new PsFake(true)
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                IOUtils.toString(take.act(new RqFake()).body())
            ),
            XhtmlMatchers.hasXPaths(
                "/xhtml:html/xhtml:body",
                "//xhtml:a[@href='https://github.com/test']",
                String.format("//xhtml:pre[.='%s']", content)
            )
        );
    }

}
