/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGitHub;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link ReleaseTag}.
 * @since 1.62
 */
final class ReleaseTagTest {

    /**
     * ReleaseTag can deny release for outdated, semantically correct versions.
     * It does however allow any version number, if it contains anything
     * other than digits and dots.
     * @throws Exception In case of error
     */
    @Test
    void validatesReleaseVersion() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        repo.releases().create("1.74");
        MatcherAssert.assertThat(
            "Greater tag should be allowed",
            new ReleaseTag(repo, "1.87.15").allowed(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            "bar tag should be allowed",
            new ReleaseTag(repo, "1.5-bar").allowed(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            "beta tag should be allowed",
            new ReleaseTag(repo, "1.9-beta").allowed(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            "Lower tag should be allowed",
            new ReleaseTag(repo, "1.62").allowed(),
            Matchers.is(false)
        );
    }

    /**
     * ReleaseTag can retrieve the latest release version in the repo.
     * @throws Exception In case of error
     */
    @Test
    void getsReferenceVersion() throws Exception {
        final Repo repo = new MkGitHub().randomRepo();
        final String latest = "2.2.1";
        repo.releases().create("1.0");
        repo.releases().create(latest);
        repo.releases().create("3.0-beta");
        MatcherAssert.assertThat(
            "Latest tag should be returned",
            new ReleaseTag(repo, "2.4").reference(),
            Matchers.is(latest)
        );
    }
}
