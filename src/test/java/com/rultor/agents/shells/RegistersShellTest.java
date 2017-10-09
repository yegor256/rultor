/**
 * Copyright (c) 2009-2017, rultor.com
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
package com.rultor.agents.shells;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import org.cactoos.text.JoinedText;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for ${@link RegistersShell}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class RegistersShellTest {

    /**
     * RegistersShell can register a shell.
     * @throws Exception In case of error.
     */
    @Test
    public void registersShell() throws Exception {
        final String host = "local";
        final int port = 221;
        final String key = "";
        final String login = "john";
        final Agent agent = new RegistersShell(
            new Profile.Fixed(
                new XMLDocument(
                    new JoinedText(
                        " ",
                        "<p><entry key='ssh'>",
                        String.format("<entry key='host'>%s</entry>", host),
                        String.format("<entry key='port'>%d</entry>", port),
                        String.format("<entry key='key'>%s</entry>", key),
                        String.format("<entry key='login'>%s</entry>", login),
                        "</entry></p>"
                    ).asString()
                )
            ),
            "localhost", 22, "rultor", "def-key"
        );
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("daemon").attr("id", "abcd")
                .add("title").set("something").up()
                .add("script").set("test")
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPaths(
                String.format("/talk/shell[@id='abcd']/host[.='%s']", host),
                String.format("/talk/shell[@id='abcd']/port[.='%d']", port),
                String.format("/talk/shell[@id='abcd']/login[.='%s']", login),
                "/talk/shell[@id='abcd']/key[.='def-key']"
            )
        );
    }

    /**
     * RegistersShell can handle broken profile.
     * @throws Exception In case of error.
     */
    @Test
    public void handlesBrokenProfileGracefully() throws Exception {
        final Profile profile = Mockito.mock(Profile.class);
        Mockito.doThrow(new Profile.ConfigException("")).when(profile).read();
        final Agent agent = new RegistersShell(
            profile, "test-host", 1, "", ""
        );
        final Talk talk = new Talk.InFile();
        agent.execute(talk);
    }

}
