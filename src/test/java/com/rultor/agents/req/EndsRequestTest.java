/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
 * @since 1.3
 */
final class EndsRequestTest {

    /**
     * EndsRequest can end a request.
     * @throws Exception In case of error.
     */
    @Test
    void endsRequest() throws Exception {
        final Agent agent = new EndsRequest();
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
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
            "Request should unsuccessfully ended",
            talk.read(),
            XhtmlMatchers.hasXPath("/talk/request[success='false']")
        );
    }

}
