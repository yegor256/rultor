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
 * @since 2.0
 */
final class IssueUrlTest {

    @Test

    void pullRequestUrlShouldBeValid() {
        MatcherAssert.assertThat(
            "It is a valid url for github PR",
            new IssueUrl("https://api.github.com/repos/USER/REPO/pulls/5086").valid(),
            Matchers.is(true)
        );
    }

    @Test
    void pullRequestReviewUrlShouldBeValid() {
        MatcherAssert.assertThat(
            "it is a valid url for file is github PR",
            new IssueUrl(
                "https://api.github.com/repos/USER/REPO/pulls/5386/files#r123"
            ).valid(),
            Matchers.is(true)
        );
    }

    @Test
    void issueUrlShouldBeValid() {
        MatcherAssert.assertThat(
            "It is a valid url for github issue",
            new IssueUrl(
                "https://api.github.com/repos/USER/REPO/issues/5182"
            ).valid(),
            Matchers.is(true)
        );
    }

    @Test
    void commitUrlShouldBeNotValid() {
        MatcherAssert.assertThat(
            "It is a valid url for github commit",
            new IssueUrl(
                "https://api.github.com/repos/USER/REPO/commit/2a1f8"
            ).valid(),
            Matchers.is(false)
        );
    }

    @Test
    void pullRequestIdShouldBeReturned() {
        MatcherAssert.assertThat(
            "Id should be parsed from PR url",
            new IssueUrl(
                "https://api.github.com/repos/USER/REPO/pulls/5186"
            ).uid(),
            Matchers.is(5186)
        );
    }

    @Test
    void issueIdShouldBeReturned() {
        MatcherAssert.assertThat(
            "Id should be parsed from issue url",
            new IssueUrl(
                "https://api.github.com/repos/USER/REPO/issues/5782"
            ).uid(),
            Matchers.is(5782)
        );
    }

    @Test
    void pullRequestIdFromReviewUrlShouldBeValid() {
        MatcherAssert.assertThat(
            "PR id should be parsed from file url",
            new IssueUrl(
                "https://api.github.com/repos/USER/REPO/pulls/5886/files#r123"
            ).uid(),
            Matchers.is(5886)
        );
    }

    @Test
    void commitIdShouldNotBeReturned() {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> new IssueUrl(
                "https://api.github.com/repos/USER/REPO/commit/2a4f8"
            ).uid(),
            "Id should not be parsed from commit url"
        );
    }
}
