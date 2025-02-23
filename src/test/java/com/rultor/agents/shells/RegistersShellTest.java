/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for ${@link RegistersShell}.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
final class RegistersShellTest {

    /**
     * RegistersShell can register a shell with hostname.
     * @throws Exception In case of error.
     */
    @Test
    void registersShell() throws Exception {
        final String host = "local";
        final int port = 221;
        final String key = "";
        final String login = "john";
        final Agent agent = new RegistersShell(
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
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
            "127.0.0.1", 22, "rultor", "def-key"
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
            "All data should be saved to shell",
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
     * RegistersShell can register shell by IP.
     * @throws Exception In case of error.
     */
    @Test
    void registerShellWithIP() {
        final String host = "192.168.5.49";
        final int port = 221;
        final String key = "";
        final String login = "john";
        Assertions.assertDoesNotThrow(
            () -> new RegistersShell(
                new Profile.Fixed(
                    new XMLDocument(
                        new Joined(
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
                host, 22, "rultor", "def-key"
            )
        );
    }

    /**
     * RegistersShell can handle broken profile.
     * @throws Exception In case of error.
     */
    @Test
    void handlesBrokenProfileGracefully() throws Exception {
        final Profile profile = Mockito.mock(Profile.class);
        Mockito.doThrow(new Profile.ConfigException("")).when(profile).read();
        final Agent agent = new RegistersShell(
            profile, "localhost", 1, "test-user", "test-key"
        );
        final Talk talk = new Talk.InFile();
        Assertions.assertDoesNotThrow(
            () -> agent.execute(talk)
        );
    }
}
