/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.io.IOException;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for ${@link Profiles}.
 * @since 1.62
 */
final class ProfilesTest {

    /**
     * Commanders section.
     */
    private static final String COMMANDERS =
        "<entry key='commanders'><item>%s</item></entry>";

    /**
     * Script section.
     */
    private static final String SCRIPT =
        "<entry key='script'><item>%s</item></entry>";

    /**
     * Commanders of merge section.
     */
    private static final String MERGE_COMMANDERS = "[merge, commanders]";

    /**
     * Profile template.
     */
    private static final String PROFILE = new Joined(
        "",
        "<p><entry key='architect'><item>%s</item></entry>",
        "<entry key='merge'>",
        ProfilesTest.COMMANDERS,
        ProfilesTest.SCRIPT,
        "</entry><entry key='deploy'>",
        ProfilesTest.COMMANDERS,
        "</entry><entry key='release'>",
        ProfilesTest.COMMANDERS,
        "</entry></p>"
    ).toString();

    /**
     * Template for exception message.
     */
    private static final String MESSAGE =
        "You cannot change `%s` section for security reasons";

    /**
     * Architect used by the merged profile fixture.
     */
    private static final String MERGED_ARCHITECT = "Yegor512";

    /**
     * Script (taken from fork) used by the merged profile fixture.
     */
    private static final String MERGED_SCRIPT = "do_another3";

    /**
     * Profiles can restrict changes in architect section.
     * @throws Exception In case of error.
     */
    @Test
    void validationFailsOnArchitectsMismatch() throws Exception {
        final String commander = "Yegor1024";
        MatcherAssert.assertThat(
            "Message should be with a reason for merge error",
            ProfilesTest.failureMessage(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            "Yegor64",
                            commander,
                            "do_something",
                            commander,
                            commander
                        )
                    )
                ),
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            "Bobby",
                            commander,
                            "do_another",
                            commander,
                            commander
                        )
                    )
                )
            ),
            Matchers.is(
                String.format(
                    ProfilesTest.MESSAGE,
                    "[architect]"
                )
            )
        );
    }

    /**
     * Profiles can restrict changes in merge/commanders section.
     * @throws Exception In case of error.
     */
    @Test
    void validationFailsOnCommandersMismatch() throws Exception {
        final String architect = "Yegor2048";
        final String commander = "Yegor4096";
        MatcherAssert.assertThat(
            "Message should be with a reason for merge error",
            ProfilesTest.failureMessage(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect,
                            "Yegor32",
                            "do_something2",
                            "Yegor16",
                            commander
                        )
                    )
                ),
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect,
                            "Bob",
                            "do_another2",
                            "Marley",
                            commander
                        )
                    )
                )
            ),
            Matchers.is(
                String.format(
                    ProfilesTest.MESSAGE,
                    ProfilesTest.MERGE_COMMANDERS
                )
            )
        );
    }

    /**
     * Profiles can restrict changes in commanders section in case when
     * overall commanders list is still the same (commander moved from one
     * section to another).
     * @throws Exception In case of error.
     */
    @Test
    void validationFailsOnCommandersMix() throws Exception {
        final String architect = "Yegor8192";
        final String first = "Commander Keen";
        final String second = "Commander Sheperd";
        MatcherAssert.assertThat(
            "Message should be with a reason for merge error",
            ProfilesTest.failureMessage(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect,
                            first,
                            "do_something4",
                            second,
                            second
                        )
                    )
                ),
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            new Joined(
                                "",
                                "<p><entry key='architect'><item>",
                                architect, "</item>",
                                "</entry><entry key='merge'>",
                                "<entry key='commanders'>",
                                "<item>", first,
                                "</item><item>",
                                second,
                                "</item><item>",
                                second,
                                "</item>",
                                "</entry>",
                                ProfilesTest.SCRIPT,
                                "</entry> </p>"
                            ).asString(),
                            "do_another4"
                        )
                    )
                )
            ),
            Matchers.is(
                String.format(
                    ProfilesTest.MESSAGE,
                    ProfilesTest.MERGE_COMMANDERS
                )
            )
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections (architect taken from master).
     * @throws Exception In case of error.
     */
    @Test
    void validationReturnsMergedArchitect() throws Exception {
        MatcherAssert.assertThat(
            "Architect is taken from master",
            ProfilesTest.mergedProfile().read().xpath(
                "//entry[@key='architect']/item/text()"
            ),
            Matchers.contains(ProfilesTest.MERGED_ARCHITECT)
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections (merge commander taken from master).
     * @throws Exception In case of error.
     */
    @Test
    void validationReturnsMergedMergeCommander() throws Exception {
        MatcherAssert.assertThat(
            "Merge commander is taken from master",
            ProfilesTest.mergedProfile().read().xpath(
                "//entry[@key='merge']/entry[@key='commanders']/item/text()"
            ),
            Matchers.contains("Total Commander")
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections (deploy commander taken from master).
     * @throws Exception In case of error.
     */
    @Test
    void validationReturnsMergedDeployCommander() throws Exception {
        MatcherAssert.assertThat(
            "Deploy commander is taken from master",
            ProfilesTest.mergedProfile().read().xpath(
                "//entry[@key='deploy']/entry[@key='commanders']/item/text()"
            ),
            Matchers.contains("Midnight Commander")
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections (release commander taken from master).
     * @throws Exception In case of error.
     */
    @Test
    void validationReturnsMergedReleaseCommander() throws Exception {
        MatcherAssert.assertThat(
            "Release commander is taken from master",
            ProfilesTest.mergedProfile().read().xpath(
                "//entry[@key='release']/entry[@key='commanders']/item/text()"
            ),
            Matchers.contains("Norton Commander")
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections (script taken from fork).
     * @throws Exception In case of error.
     */
    @Test
    void validationReturnsMergedScript() throws Exception {
        MatcherAssert.assertThat(
            "Script is taken from fork",
            ProfilesTest.mergedProfile().read().xpath(
                "//entry[@key='merge']/entry[@key='script']/item/text()"
            ),
            Matchers.contains(ProfilesTest.MERGED_SCRIPT)
        );
    }

    /**
     * Build a validated merged profile from master and fork profiles
     * using shared constants for commanders.
     * @return Validated merged profile
     * @throws Exception In case of error.
     */
    private static Profile mergedProfile() throws Exception {
        final String first = "Total Commander";
        final String second = "Midnight Commander";
        final String third = "Norton Commander";
        return new Profiles().validated(
            new Profile.Fixed(
                new XMLDocument(
                    String.format(
                        ProfilesTest.PROFILE,
                        ProfilesTest.MERGED_ARCHITECT,
                        first,
                        "do_something3",
                        second,
                        third
                    )
                )
            ),
            new Profile.Fixed(
                new XMLDocument(
                    String.format(
                        ProfilesTest.PROFILE,
                        ProfilesTest.MERGED_ARCHITECT,
                        first,
                        ProfilesTest.MERGED_SCRIPT,
                        second,
                        third
                    )
                )
            )
        );
    }

    /**
     * Run validation that is expected to fail and return the exception
     * message. Returns null if no exception was thrown.
     * @param master Master profile
     * @param fork Fork profile
     * @return Exception message
     */
    private static String failureMessage(
        final Profile master, final Profile fork
    ) throws IOException {
        String message = null;
        try {
            new Profiles().validated(master, fork);
        } catch (final Profile.ConfigException exception) {
            message = exception.getMessage();
        }
        return message;
    }
}
