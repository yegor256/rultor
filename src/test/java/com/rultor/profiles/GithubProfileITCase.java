/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.github.Coordinates;
import com.jcabi.github.RtGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Profile;
import com.yegor256.WeAreOnline;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for {@link GithubProfile}.
 *
 * @since 1.0
 */
@SuppressWarnings("PMD.UseUtilityClass")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(WeAreOnline.class)
final class GithubProfileITCase {

    /**
     * GithubProfile can fetch a YAML config.
     * @throws Exception In case of error.
     */
    @Test
    @Disabled
    void fetchesYamlConfig() throws Exception {
        final Profile profile = new GithubProfile(
            new RtGitHub().repos().get(
                new Coordinates.Simple("yegor256/rultor")
            )
        );
        MatcherAssert.assertThat(
            "script for merge should be read",
            profile.read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='merge']/entry[@key='script']"
            )
        );
    }
}
