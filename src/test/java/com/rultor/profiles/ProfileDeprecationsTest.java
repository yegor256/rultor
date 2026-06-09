/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link ProfileDeprecations}.
 * @since 1.62
 */
final class ProfileDeprecationsTest {

    /**
     * The format of an profile that defines the docker image to use.
     */
    private static final String PROFILE_FORMAT = new Joined(
        "",
        "<p><entry key='docker'>",
        "<entry key='image'>%s</entry>",
        "</entry></p>"
    ).toString();

    /**
     * ProfileDeprecations can identify a deprecated profile by missing config.
     * @throws Exception In case of error
     */
    @Test
    void identifiesDeprecatedProfileByMissingConfig() throws Exception {
        MatcherAssert.assertThat(
            "Deprecated merge, release, deploy should be in the list",
            new ProfileDeprecations(new Profile.Fixed()).empty(),
            Matchers.is(false)
        );
    }

    /**
     * ProfileDeprecations can identify a deprecated profile by docker image.
     * @throws Exception In case of error
     */
    @Test
    void identifiesDeprecatedProfileByDockerImage() throws Exception {
        MatcherAssert.assertThat(
            "Deprecated image should be in the list",
            new ProfileDeprecations(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfileDeprecationsTest.PROFILE_FORMAT,
                            "yegor256/rultor-image"
                        )
                    )
                )
            ).empty(),
            Matchers.is(false)
        );
    }

    /**
     * ProfileDeprecations can identify a valid profile.
     * @throws Exception In case of error
     */
    @Test
    void identifiesValidProfile() throws Exception {
        MatcherAssert.assertThat(
            "Deprecation list should be empty",
            new ProfileDeprecations(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfileDeprecationsTest.PROFILE_FORMAT,
                            "foo"
                        )
                    )
                )
            ).empty(),
            Matchers.is(true)
        );
    }
}
