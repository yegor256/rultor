/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link Dephantomizes}.
 *
 * @since 1.59.7
 */
final class DephantomizesTest {

    /**
     * Dephantomizes can remove request and wire.
     * @throws IOException In case of error
     */
    @Test
    void removesRequestAndWire() throws IOException {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        final Talk talk = DephantomizesTest.talk(repo, 0);
        new Dephantomizes(github).execute(talk);
        MatcherAssert.assertThat(
            "Request and wire should be removed",
            talk.read(),
            XhtmlMatchers.hasXPath("/talk[not(request) and not(wire)]")
        );
    }

    /**
     * Dephantomizes can remove request and wire.
     * @throws IOException In case of error
     */
    @Test
    void doesNotTouchRequestAndWire() throws IOException {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        repo.issues().create("title", "desc");
        final Talk talk = DephantomizesTest.talk(repo, 1);
        new Dephantomizes(github).execute(talk);
        MatcherAssert.assertThat(
            "Request and wire should not be removed",
            talk.read(),
            XhtmlMatchers.hasXPath("/talk[request and wire]")
        );
    }

    /**
     * Make a fake talk.
     * @param repo Repo
     * @param issue The issue
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(final Repo repo, final int issue)
        throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("wire").add("href").set("#").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue)).up().up()
                .add("request").attr("id", "a1b2c3d4")
                .add("author").set("yegor256").up()
                .add("type").set("deploy").up()
                .add("args")
        );
        return talk;
    }

}
