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
package com.rultor.web;

import com.jcabi.matchers.JaxbConverter;
import com.jcabi.matchers.XhtmlMatchers;
import com.rexsl.mock.HttpHeadersMocker;
import com.rexsl.mock.MkServletContext;
import com.rexsl.mock.UriInfoMocker;
import com.rultor.spi.Talks;
import javax.ws.rs.core.SecurityContext;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Test case for {@link SiblingsRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.23.1
 */
public final class SiblingsRsTest {

    /**
     * SiblingsRs can render a list.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersListOfTalks() throws Exception {
        final SiblingsRs home = new SiblingsRs();
        home.setName("test");
        home.setUriInfo(new UriInfoMocker().mock());
        home.setHttpHeaders(new HttpHeadersMocker().mock());
        home.setSecurityContext(Mockito.mock(SecurityContext.class));
        final Talks talks = new Talks.InDir();
        final String name = "hello";
        talks.create("repo1", name);
        talks.get(name).modify(
            new Directives()
                .xpath("/talk")
                .add("wire").add("href").set("http://example.com").up().up()
                .add("archive").add("log").attr("title", "hello, world")
                .attr("id", "a1b2c3").set("s3://")
        );
        home.setServletContext(
            new MkServletContext().withAttr(Talks.class.getName(), talks)
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(home.index("123").getEntity()),
            XhtmlMatchers.hasXPaths(
                "/page[repo='test']",
                "/page[since='123']",
                "/page/siblings[count(talk)=1]",
                "/page/siblings/talk/archive/log[id and href and title]",
                "/page/siblings/talk/archive[count(log)=1]",
                "//log[starts-with(href,'http://')]"
            )
        );
    }

}
