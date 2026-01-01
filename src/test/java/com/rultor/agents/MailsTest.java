/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.xml.XMLDocument;
import com.rultor.Env;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for ${@link Mails}.
 *
 * @since 2.0
 */
final class MailsTest {

    /**
     * Mails can send a mail.
     * @throws Exception In case of error.
     */
    @Test
    @Disabled
    void sendsMail() throws Exception {
        final Postman postman = Mockito.spy(Postman.CONSOLE);
        final Agent agent = new Mails(
            this.profile(),
            postman
        );
        agent.execute(MailsTest.talk());
        final ArgumentCaptor<Envelope> captor =
            ArgumentCaptor.forClass(Envelope.class);
        Mockito.verify(postman).send(captor.capture());
        final Envelope envelope = captor.getValue();
        MatcherAssert.assertThat(
            "Mail text should contain some data",
            envelope.unwrap().getContent().toString(),
            Matchers.allOf(
                Matchers.containsString("See #456, release log:"),
                Matchers.containsString("Released by Rultor"),
                Matchers.containsString(Env.read("Rultor-Version")),
                Matchers.containsString(
                    "see [build log](https://www.rultor.com/t/123-abcdef)"
                )
            )
        );
        MatcherAssert.assertThat(
            "Mail subject should be about release",
            envelope.unwrap().getSubject(),
            Matchers.equalTo("user/repo v2.0 released!")
        );
    }

    /**
     * Mails can send a mail to recipients.
     * @throws Exception In case of error.
     * @todo #748 Implement method sendsToRecipients. It must check that
     *  mail is sent to all recipients. Recipients are defined in Profile.
     */
    @Test
    @Disabled
    void sendsToRecipients() {
        Assertions.assertDoesNotThrow(
            () ->
                !"test".equalsIgnoreCase("implemented")
        );
    }

    /**
     * Profile for test.
     * @return Profile
     */
    private Profile.Fixed profile() throws Exception {
        return new Profile.Fixed(
            new XMLDocument(
                new Joined(
                    "",
                    "<p><entry key='release'>",
                    "<entry key='email'>",
                    "<item>test1@localhost</item>",
                    "<item>test2@localhost</item>",
                    "</entry>",
                    "</entry></p>"
                ).asString()
            )
        );
    }

    /**
     * Make a talk with this tag.
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk() throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .attr("number", "123")
                .add("wire")
                .add("github-issue").set("456")
                .add("github-repo").set("user/repo")
                .up()
                .up()
                .add("request").attr("id", "abcdef")
                .add("type").set("release").up()
                .add("success").set("true").up()
                .add("args").add("arg").attr("name", "tag").set("v2.0")
        );
        return talk;
    }
}
