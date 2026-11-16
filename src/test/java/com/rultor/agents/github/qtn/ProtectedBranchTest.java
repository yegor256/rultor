/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkBranches;
import com.jcabi.github.mock.MkGitHub;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ProtectedBranch}.
 *
 * @since 2.0
 */
final class ProtectedBranchTest {

    /**
     * Branch protection response with reviews and status checks
     * required, as GitHub returns it for a protected branch.
     */
    private static final String PROTECTED = String.join(
        "",
        "{\"required_pull_request_reviews\":",
        "{\"dismiss_stale_reviews\":false,",
        "\"required_approving_review_count\":1},",
        "\"required_status_checks\":{\"strict\":true,",
        "\"contexts\":[],\"checks\":[]},",
        "\"enforce_admins\":{\"enabled\":false},",
        "\"required_signatures\":{\"enabled\":false},",
        "\"required_linear_history\":{\"enabled\":false},",
        "\"lock_branch\":{\"enabled\":false}}"
    );

    /**
     * Branch protection response with the branch locked.
     */
    private static final String LOCKED = String.join(
        "",
        "{\"required_pull_request_reviews\":null,",
        "\"required_status_checks\":null,",
        "\"enforce_admins\":{\"enabled\":false},",
        "\"required_signatures\":{\"enabled\":false},",
        "\"required_linear_history\":{\"enabled\":false},",
        "\"lock_branch\":{\"enabled\":true}}"
    );

    /**
     * ProtectedBranch names the rules enabled by GitHub.
     */
    @Test
    void namesEnabledRules() {
        MatcherAssert.assertThat(
            "Enabled protection rules should be named",
            ProtectedBranch.rules(
                HttpURLConnection.HTTP_OK,
                ProtectedBranchTest.PROTECTED
            ),
            Matchers.allOf(
                Matchers.containsString("required status checks"),
                Matchers.containsString("required pull request reviews")
            )
        );
    }

    /**
     * ProtectedBranch names a locked branch.
     */
    @Test
    void namesLockedBranch() {
        MatcherAssert.assertThat(
            "Locked branch should be reported",
            ProtectedBranch.rules(
                HttpURLConnection.HTTP_OK,
                ProtectedBranchTest.LOCKED
            ),
            Matchers.containsString("the branch is locked")
        );
    }

    /**
     * ProtectedBranch treats HTTP 404 as an unprotected branch.
     */
    @Test
    void treatsNotFoundAsUnprotected() {
        MatcherAssert.assertThat(
            "Unprotected branch should not block the merge",
            ProtectedBranch.rules(
                HttpURLConnection.HTTP_NOT_FOUND,
                "{}"
            ),
            Matchers.is("")
        );
    }

    /**
     * ProtectedBranch proceeds when the response carries no
     * protection payload.
     */
    @Test
    void proceedsWhenPayloadIsEmpty() {
        MatcherAssert.assertThat(
            "Empty protection payload should not block the merge",
            ProtectedBranch.rules(HttpURLConnection.HTTP_OK, "{}"),
            Matchers.is("")
        );
    }

    /**
     * ProtectedBranch proceeds when GitHub answers unexpectedly.
     */
    @Test
    void proceedsWhenStatusIsUnexpected() {
        MatcherAssert.assertThat(
            "Unexpected status should not block the merge",
            ProtectedBranch.rules(
                HttpURLConnection.HTTP_INTERNAL_ERROR,
                ""
            ),
            Matchers.is("")
        );
    }

    /**
     * ProtectedBranch explains the blocked merge for a protected branch.
     */
    @Test
    void explainsBlockedBranch() {
        MatcherAssert.assertThat(
            "Explanation should name the branch and the rules",
            ProtectedBranch.blocker(
                HttpURLConnection.HTTP_OK,
                ProtectedBranchTest.PROTECTED,
                "master"
            ),
            Matchers.allOf(
                Matchers.containsString("`master`"),
                Matchers.containsString("required status checks"),
                Matchers.containsString(
                    "GitHub branch protection rules"
                )
            )
        );
    }

    /**
     * ProtectedBranch stays quiet when the branch is not protected.
     */
    @Test
    void staysQuietWhenUnprotected() {
        MatcherAssert.assertThat(
            "Explanation should be empty for unprotected branch",
            ProtectedBranch.blocker(
                HttpURLConnection.HTTP_NOT_FOUND,
                "{}",
                "master"
            ),
            Matchers.is("")
        );
    }

    /**
     * ProtectedBranch proceeds for a pull request in a repo without
     * protection info, e.g. when the branch state can't be determined.
     *
     * @throws Exception In case of error
     */
    @Test
    void proceedsForRealPullWithoutProtection() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final MkBranches branches = (MkBranches) repo.branches();
        branches.create("head", "abcdef4");
        branches.create("base", "abcdef5");
        MatcherAssert.assertThat(
            "Merge should proceed when protection is unknown",
            new ProtectedBranch(repo.pulls().create("", "head", "base"))
                .blocker(),
            Matchers.is("")
        );
    }
}
