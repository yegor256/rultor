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
import org.junit.jupiter.api.Assertions;
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
     * Profiles can restrict changes in architect section.
     * @throws Exception In case of error.
     */
    @Test
    void validationFailsOnArchitectsMismatch() throws Exception {
        final String commander = "Yegor1024";
        final Profile master = new Profile.Fixed(
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
        );
        MatcherAssert.assertThat(
            "Message should be with a reason for merge error",
            Assertions.assertThrows(
                Profile.ConfigException.class,
                () -> new Profiles().validated(
                    master,
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
                )
            ).getMessage(),
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
        final Profile master = new Profile.Fixed(
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
        );
        MatcherAssert.assertThat(
            "Message should be with a reason for merge error",
            Assertions.assertThrows(
                Profile.ConfigException.class,
                () -> new Profiles().validated(
                    master,
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
                )
            ).getMessage(),
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
        final Profile master = new Profile.Fixed(
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
        );
        MatcherAssert.assertThat(
            "Message should be with a reason for merge error",
            Assertions.assertThrows(
                Profile.ConfigException.class,
                () -> new Profiles().validated(
                    master,
                    ProfilesTest.commandersMixFork(architect, first, second)
                )
            ).getMessage(),
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
     * sections, keeping the architect from master.
     * @throws Exception In case of error.
     */
    @Test
    void validationKeepsArchitectFromMaster() throws Exception {
        final String architect = "Yegor512";
        final String first = "Total Commander";
        final String second = "Midnight Commander";
        final String third = "Norton Commander";
        MatcherAssert.assertThat(
            "Architect is taken from master",
            new Profiles().validated(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_something3", second, third
                        )
                    )
                ),
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_another3", second, third
                        )
                    )
                )
            ).read().xpath("//entry[@key='architect']/item/text()"),
            Matchers.contains(architect)
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections, keeping the merge commander from master.
     * @throws Exception In case of error.
     */
    @Test
    void validationKeepsMergeCommanderFromMaster() throws Exception {
        final String architect = "Yegor512";
        final String first = "Total Commander";
        final String second = "Midnight Commander";
        final String third = "Norton Commander";
        MatcherAssert.assertThat(
            "Merge commander is taken from master",
            new Profiles().validated(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_something3", second, third
                        )
                    )
                ),
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_another3", second, third
                        )
                    )
                )
            ).read().xpath(
                String.format(
                    "//entry[@key='%s']/entry[@key='commanders']/item/text()",
                    "merge"
                )
            ),
            Matchers.contains(first)
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections, keeping the deploy commander from master.
     * @throws Exception In case of error.
     */
    @Test
    void validationKeepsDeployCommanderFromMaster() throws Exception {
        final String architect = "Yegor512";
        final String first = "Total Commander";
        final String second = "Midnight Commander";
        final String third = "Norton Commander";
        MatcherAssert.assertThat(
            "Deploy commander is taken from master",
            new Profiles().validated(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_something3", second, third
                        )
                    )
                ),
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_another3", second, third
                        )
                    )
                )
            ).read().xpath(
                String.format(
                    "//entry[@key='%s']/entry[@key='commanders']/item/text()",
                    "deploy"
                )
            ),
            Matchers.contains(second)
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections, keeping the release commander from master.
     * @throws Exception In case of error.
     */
    @Test
    void validationKeepsReleaseCommanderFromMaster() throws Exception {
        final String architect = "Yegor512";
        final String first = "Total Commander";
        final String second = "Midnight Commander";
        final String third = "Norton Commander";
        MatcherAssert.assertThat(
            "Release commander is taken from master",
            new Profiles().validated(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_something3", second, third
                        )
                    )
                ),
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_another3", second, third
                        )
                    )
                )
            ).read().xpath(
                String.format(
                    "//entry[@key='%s']/entry[@key='commanders']/item/text()",
                    "release"
                )
            ),
            Matchers.contains(third)
        );
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections, taking the script from fork.
     * @throws Exception In case of error.
     */
    @Test
    void validationTakesScriptFromFork() throws Exception {
        final String architect = "Yegor512";
        final String first = "Total Commander";
        final String second = "Midnight Commander";
        final String third = "Norton Commander";
        final String script = "do_another3";
        MatcherAssert.assertThat(
            "Script is taken from fork",
            new Profiles().validated(
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, "do_something3", second, third
                        )
                    )
                ),
                new Profile.Fixed(
                    new XMLDocument(
                        String.format(
                            ProfilesTest.PROFILE,
                            architect, first, script, second, third
                        )
                    )
                )
            ).read().xpath(
                "//entry[@key='merge']/entry[@key='script']/item/text()"
            ),
            Matchers.contains(script)
        );
    }

    private static Profile commandersMixFork(final String architect,
        final String first, final String second) throws Exception {
        return new Profile.Fixed(
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
        );
    }
}
