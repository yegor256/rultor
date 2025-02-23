/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link StartsTalks}.
 *
 * @since 1.9
 */
final class StartsTalksTest {

    /**
     * StartsTalks can start a talk.
     * @throws Exception In case of error.
     */
    @Test
    @Disabled
    void startsTalks() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("hey, do it");
        final SuperAgent agent = new StartsTalks(repo.github());
        final Talks talks = new Talks.InDir();
        agent.execute(talks);
        MatcherAssert.assertThat(
            "Active talk should not be created",
            talks.active(),
            Matchers.not(Matchers.emptyIterable())
        );
    }

}
