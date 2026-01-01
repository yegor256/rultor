/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link IssueUrl}.
 *
 * @since 2.0
 */
final class IssueUrlTest {
    @Test
    void pullRequestUrlShouldBeValid() {
        final IssueUrl issue =
            new IssueUrl("https://api.github.com/repos/USER/REPO/pulls/5086");
        MatcherAssert.assertThat(
            "It is a valid url for github PR",
            issue.valid(),
            Matchers.is(true)
        );
    }

    @Test
    void pullRequestReviewUrlShouldBeValid() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/pulls/5386/files#r123"
        );
        MatcherAssert.assertThat(
            "it is a valid url for file is github PR",
            issue.valid(),
            Matchers.is(true)
        );
    }

    @Test
    void issueUrlShouldBeValid() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/issues/5182"
        );
        MatcherAssert.assertThat(
            "It is a valid url for github issue",
            issue.valid(),
            Matchers.is(true)
        );
    }

    @Test
    void commitUrlShouldBeNotValid() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/commit/2a1f8"
        );
        MatcherAssert.assertThat(
            "It is a valid url for github commit",
            issue.valid(),
            Matchers.is(false)
        );
    }

    @Test
    void pullRequestIdShouldBeReturned() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/pulls/5186"
        );
        MatcherAssert.assertThat(
            "Id should be parsed from PR url",
            issue.uid(),
            Matchers.is(5186)
        );
    }

    @Test
    void issueIdShouldBeReturned() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/issues/5782"
        );
        MatcherAssert.assertThat(
            "Id should be parsed from issue url",
            issue.uid(),
            Matchers.is(5782)
        );
    }

    @Test
    void pullRequestIdFromReviewUrlShouldBeValid() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/pulls/5886/files#r123"
        );
        MatcherAssert.assertThat(
            "PR id should be parsed from file url",
            issue.uid(),
            Matchers.is(5886)
        );
    }

    @Test
    void commitIdShouldNotBeReturned() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/commit/2a4f8"
        );
        Assertions.assertThrows(
            IllegalStateException.class,
            issue::uid,
            "Id should not be parsed from commit url"
        );
    }
}
