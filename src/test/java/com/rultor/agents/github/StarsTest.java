/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for {@link Stars}.
 *
 * @since 1.1
 */
final class StarsTest {
    /**
     * Stars can star a new repo.
     * @throws IOException In case of error
     */
    @Test
    void starsNewRepo() throws IOException {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        final Talk talk = this.talk(repo);
        new Stars(github).execute(talk);
        MatcherAssert.assertThat(
            "Star should be added to the repo",
            repo.stars().starred(),
            Matchers.is(true)
        );
    }

    /**
     * Stars should leave already starred repo.
     * @throws IOException In case of error
     */
    @Test
    void leavesStarredRepo() throws IOException {
        final MkGitHub github = new MkGitHub();
        final Repo repo = github.randomRepo();
        final Talk talk = this.talk(repo);
        repo.stars().star();
        new Stars(github).execute(talk);
        MatcherAssert.assertThat(
            "Star should be kept if already stared",
            repo.stars().starred(),
            Matchers.is(true)
        );
    }

    /**
     * Create a test talk.
     * @param repo Repo to use
     * @return Test Talk.
     * @throws IOException In case of error.
     */
    private Talk talk(final Repo repo) throws IOException {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("wire").add("href").set("#").up()
                .add("github-repo").set(repo.coordinates().toString()).up()
        );
        return talk;
    }
}
