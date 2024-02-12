/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.agents.github;

import com.jcabi.github.Repo;
import com.jcabi.github.RepoCommit;
import com.jcabi.github.RepoCommits;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import javax.json.Json;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for ${@link CommitsLog}.
 *
 * @since 1.51
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
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
