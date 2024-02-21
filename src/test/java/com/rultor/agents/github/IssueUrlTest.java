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
            new IssueUrl("https://api.github.com/repos/USER/REPO/pull/5086");
        MatcherAssert.assertThat(
            issue.valid(),
            Matchers.is(true)
        );
    }

    @Test
    void pullRequestReviewUrlShouldBeValid() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/pull/5386/files#r123"
        );
        MatcherAssert.assertThat(
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
            issue.valid(),
            Matchers.is(false)
        );
    }

    @Test
    void pullRequestIdShouldBeReturned() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/pull/5186"
        );
        MatcherAssert.assertThat(
            issue.id(),
            Matchers.is(5186)
        );
    }

    @Test
    void issueIdShouldBeReturned() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/issues/5782"
        );
        MatcherAssert.assertThat(
            issue.id(),
            Matchers.is(5782)
        );
    }

    @Test
    void pullRequestIdFromReviewUrlShouldBeValid() {
        final IssueUrl issue = new IssueUrl(
            "https://api.github.com/repos/USER/REPO/pull/5886/files#r123"
        );
        MatcherAssert.assertThat(
            issue.id(),
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
            issue::id
        );
    }

    @Test
    void urlShouldNotBeEmpty() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new IssueUrl("")
        );
    }

    @Test
    void urlShouldNotBeNull() {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> new IssueUrl(null)
        );
    }
}
