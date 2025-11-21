/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.github.Repos;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Agent;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link Publishes}.
 *
 * @since 1.32.7
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class PublishesTest {

    /**
     * Publishes can add a public attribute.
     * @throws Exception In case of error.
     */
    @Test
    void addsPublicAttribute() throws Exception {
        final MkGitHub github = new MkGitHub("test");
        github.repos().create(new Repos.RepoCreate("test", false));
        final Agent agent = new Publishes(new Profile.Fixed(), github);
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk").add("archive")
                .add("log").attr("id", "abc").attr("title", "hey").up()
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "public attribute should be added",
            talk.read(),
            XhtmlMatchers.hasXPath("/talk[@public='true']")
        );
    }

    /**
     * Publishes can ignore if PUBLIC attribute is already set.
     * @throws Exception In case of error.
     */
    @Test
    void ignoresIfPublicAttributeSet() throws Exception {
        final Agent agent = new Publishes(new Profile.Fixed(), new MkGitHub());
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .attr("public", "false")
                .add("archive")
                .add("log").attr("id", "abc").attr("title", "hey").up()
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            "public attribute should be kept false",
            talk.read(),
            XhtmlMatchers.hasXPath("/talk[@public='false']")
        );
    }

}
