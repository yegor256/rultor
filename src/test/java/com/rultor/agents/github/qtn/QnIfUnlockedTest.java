/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkBranches;
import com.jcabi.github.mock.MkGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for ${@link QnIfUnlocked}.
 *
 * @since 1.53
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
final class QnIfUnlockedTest {

    /**
     * QnIfUnlocked can build a request.
     * @throws Exception In case of error.
     */
    @Test
    void buildsRequest() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "dfgsadf4");
        branches.create("base", "retygdy6");
        final Issue issue = repo.issues().get(
            repo.pulls().create("", "head", "base").number()
        );
        issue.comments().post("merge");
        MatcherAssert.assertThat(
            "Merge request should be created if not locked",
            new Xembler(
                new Directives().add("request").append(
                    new QnIfUnlocked(new QnMerge()).understand(
                        new Comment.Smart(issue.comments().get(1)), new URI("#")
                    ).dirs()
                )
            ).xml(),
            XhtmlMatchers.hasXPath("/request/type[text()='merge']")
        );
    }

}
