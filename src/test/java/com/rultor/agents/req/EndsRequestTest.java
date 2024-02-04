/**
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.agents.req;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link EndsRequest}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.3
 */
public final class EndsRequestTest {

    /**
     * EndsRequest can end a request.
     * @throws Exception In case of error.
     */
    @Test
    public void endsRequest() throws Exception {
        final Agent agent = new EndsRequest();
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                // @checkstyle MultipleStringLiteralsCheck (1 line)
                .add("daemon").attr("id", "abcd")
                .add("title").set("some operation").up()
                .add("script").set("test").up()
                .add("code").set("13").up()
                .add("started").set("2013-01-01T11:35:09Z").up()
                .add("ended").set("2013-01-01T12:35:09Z").up().up()
                .add("request").attr("id", "1")
                .add("author").set("yegor256").up()
                .add("type").set("something").up()
                .add("args")
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPath("/talk/request[success='false']")
        );
    }

}
