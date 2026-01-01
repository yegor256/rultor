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
     * ProfileDeprecations can identify a deprecated profile.
     * @throws Exception In case of error
     */
    @Test
    void identifiesDeprecatedProfile() throws Exception {
        ProfileDeprecations deprecations = new ProfileDeprecations(
            new Profile.Fixed()
        );
        MatcherAssert.assertThat(
            "Deprecated merge, release, deploy should be in the list",
            deprecations.empty(),
            Matchers.is(false)
        );
        deprecations = new ProfileDeprecations(
            new Profile.Fixed(
                new XMLDocument(
                    String.format(
                        ProfileDeprecationsTest.PROFILE_FORMAT,
                        "yegor256/rultor-image"
                    )
                )
            )
        );
        MatcherAssert.assertThat(
            "Deprecated image should be in the list",
            deprecations.empty(),
            Matchers.is(false)
        );
    }

    /**
     * ProfileDeprecations can identify a valid profile.
     * @throws Exception In case of error
     */
    @Test
    void identifiesValidProfile() throws Exception {
        final ProfileDeprecations deprecations = new ProfileDeprecations(
            new Profile.Fixed(
                new XMLDocument(
                    String.format(
                        ProfileDeprecationsTest.PROFILE_FORMAT,
                        "foo"
                    )
                )
            )
        );
        MatcherAssert.assertThat(
            "Deprecation list should be empty",
            deprecations.empty(),
            Matchers.is(true)
        );
    }
}
