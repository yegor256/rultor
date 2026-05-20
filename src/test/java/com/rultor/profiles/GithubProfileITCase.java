/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.github.Coordinates;
import com.jcabi.github.RtGitHub;
import com.jcabi.matchers.XhtmlMatchers;
import com.yegor256.WeAreOnline;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for {@link GithubProfile}.
 * @since 1.0
 */
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
        MatcherAssert.assertThat(
            "script for merge should be read",
            GithubProfile.fromRepo(
                new RtGitHub().repos().get(
                    new Coordinates.Simple("yegor256/rultor")
                )
            ).read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='merge']/entry[@key='script']"
            )
        );
    }
}
