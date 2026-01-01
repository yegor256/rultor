/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import co.stateful.mock.MkSttc;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link UnlocksRepo}.
 *
 * @since 1.22.1
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class UnlocksRepoTest {

    /**
     * UnlocksRepo can unlock a repo.
     * @throws Exception In case of error.
     */
    @Test
    void unlocksRepo() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("hey, do it");
        final SuperAgent agent = new UnlocksRepo(
            new MkSttc().locks(), repo.github()
        );
        final Talks talks = new Talks.InDir();
        final String name = "test-talk";
        talks.create("", name);
        talks.get(name).modify(
            new Directives()
                .xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire")
                .add("github-repo").set(repo.coordinates().toString()).up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
                .add("href").set("#").up()
        );
        Assertions.assertDoesNotThrow(
            () -> agent.execute(talks)
        );
    }

}
