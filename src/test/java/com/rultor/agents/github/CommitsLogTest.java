/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Repo;
import com.jcabi.github.RepoCommit;
import com.jcabi.github.RepoCommits;
import jakarta.json.Json;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for ${@link CommitsLog}.
 *
 * @since 1.51
 */
final class CommitsLogTest {

    /**
     * CommitsLog can create a log for release.
     * @throws Exception In case of error.
     */
    @Test
    @SuppressWarnings("unchecked")
    void createsReleaseLog() throws Exception {
        final RepoCommits commits = Mockito.mock(RepoCommits.class);
        Mockito.doReturn(
            Collections.singleton(
                this.commit(
                    "hi\u20ac\t\n this is a very long commit message"
                )
            )
        ).when(commits).iterate(Mockito.any(Map.class));
        final Repo repo = Mockito.mock(Repo.class);
        Mockito.doReturn(commits).when(repo).commits();
        MatcherAssert.assertThat(
            "Message should be shorter",
            new CommitsLog(repo).build(new Date(), new Date()),
            Matchers.equalTo(
                " * a1b2c3 by @jeff: hi\u20ac this is a very long commit..."
            )
        );
    }

    /**
     * CommitsLog can create a log for release.
     * @throws Exception In case of error.
     */
    @Test
    @SuppressWarnings("unchecked")
    void createsLongReleaseLog() throws Exception {
        final RepoCommits commits = Mockito.mock(RepoCommits.class);
        final Collection<RepoCommit> list = new LinkedList<>();
        for (int idx = 0; idx < 100; ++idx) {
            list.add(this.commit(String.format("commit #%d", idx)));
        }
        Mockito.doReturn(list).when(commits).iterate(Mockito.any(Map.class));
        final Repo repo = Mockito.mock(Repo.class);
        Mockito.doReturn(commits).when(repo).commits();
        MatcherAssert.assertThat(
            "Only 20 commits should be mentioned directly",
            new CommitsLog(repo).build(new Date(), new Date()),
            Matchers.containsString("* and 80 more..")
        );
    }

    /**
     * Create repo commit.
     * @param msg Message
     * @return Commit
     * @throws IOException In case of error.
     */
    private RepoCommit commit(final String msg) throws IOException {
        final RepoCommit commit = Mockito.mock(RepoCommit.class);
        Mockito.doReturn("a1b2c3").when(commit).sha();
        Mockito.doReturn(
            Json.createObjectBuilder()
                .add("author", Json.createObjectBuilder().add("login", "jeff"))
                .add("commit", Json.createObjectBuilder().add("message", msg))
                .build()
        ).when(commit).json();
        return commit;
    }

}
