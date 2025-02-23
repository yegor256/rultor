/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.rultor.agents.github.Req;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link QnLock}.
 *
 * @since 1.53
 */
final class QnLockTest {

    /**
     * QnLock can lock a branch.
     * @throws Exception In case of error.
     */
    @Test
    void locksBranch() throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        issue.comments().post("lock users=`@test1, test2`");
        MatcherAssert.assertThat(
            "Command should be marked as done",
            new QnLock().understand(
                new Comment.Smart(issue.comments().get(1)), new URI("#")
            ),
            Matchers.is(Req.DONE)
        );
        MatcherAssert.assertThat(
            "Message about action should be posted",
            new Comment.Smart(issue.comments().get(2)).body(),
            Matchers.allOf(
                Matchers.containsString(
                    "I added `.rultor.lock` file to the `master` branch"
                ),
                Matchers.containsString("only @jeff, @test1, @test2 can merge")
            )
        );
    }

}
