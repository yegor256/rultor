/*
 * Copyright (c) 2009-2025 Yegor Bugayenko
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
package com.rultor.profiles;

import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.util.List;
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
        final Profile merged = new Profile.Fixed(
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
        );
        try {
            new Profiles().validated(master, merged);
            Assertions.fail("Code above must throw an exception");
        } catch (final Profile.ConfigException exception) {
            MatcherAssert.assertThat(
                "Message should be with a reason for merge error",
                exception.getMessage(),
                Matchers.is(
                    String.format(
                        ProfilesTest.MESSAGE,
                        "[architect]"
                    )
                )
            );
        }
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
        final Profile merged = new Profile.Fixed(
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
        );
        try {
            new Profiles().validated(master, merged);
            Assertions.fail("Method above must throw an exception");
        } catch (final Profile.ConfigException exception) {
            MatcherAssert.assertThat(
                "Message should be with a reason for merge error",
                exception.getMessage(),
                Matchers.is(
                    String.format(
                        ProfilesTest.MESSAGE,
                        ProfilesTest.MERGE_COMMANDERS
                    )
                )
            );
        }
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
        final String second = "Commander Shepard";
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
        final Profile merged = new Profile.Fixed(
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
        try {
            new Profiles().validated(master, merged);
            Assertions.fail("Line above must throw an exception");
        } catch (final Profile.ConfigException exception) {
            MatcherAssert.assertThat(
                "Message should be with a reason for merge error",
                exception.getMessage(),
                Matchers.is(
                    String.format(
                        ProfilesTest.MESSAGE,
                        ProfilesTest.MERGE_COMMANDERS
                    )
                )
            );
        }
    }

    /**
     * Profiles can validate merged profile without changes in restricted
     * sections.
     * @throws Exception In case of error.
     */
    @Test
    void validationReturnsMerged() throws Exception {
        final String architect = "Yegor512";
        final String first = "Total Commander";
        final String second = "Midnight Commander";
        final String third = "Norton Commander";
        final String script = "do_another3";
        final Profile master = new Profile.Fixed(
            new XMLDocument(
                String.format(
                    ProfilesTest.PROFILE,
                    architect,
                    first,
                    "do_something3",
                    second,
                    third
                )
            )
        );
        final Profile fork = new Profile.Fixed(
            new XMLDocument(
                String.format(
                    ProfilesTest.PROFILE,
                    architect,
                    first,
                    script,
                    second,
                    third
                )
            )
        );
        final Profile validated = new Profiles().validated(master, fork);
        final List<String> architects = validated.read().xpath(
            "//entry[@key='architect']/item/text()"
        );
        MatcherAssert.assertThat(
            "Architect is taken from master",
            architects,
            Matchers.contains(architect)
        );
        final String path =
            "//entry[@key='%s']/entry[@key='commanders']/item/text()";
        final List<String> merge = validated.read().xpath(
            String.format(path, "merge")
        );
        MatcherAssert.assertThat(
            "Merge commander is taken from master",
            merge,
            Matchers.contains(first)
        );
        final List<String> deploy = validated.read().xpath(
            String.format(path, "deploy")
        );
        MatcherAssert.assertThat(
            "Deploy commander is taken from master",
            deploy,
            Matchers.contains(second)
        );
        final List<String> release = validated.read().xpath(
            String.format(path, "release")
        );
        MatcherAssert.assertThat(
            "Release commander is taken from master",
            release,
            Matchers.contains(third)
        );
        final List<String> scripts = validated.read().xpath(
            "//entry[@key='merge']/entry[@key='script']/item/text()"
        );
        MatcherAssert.assertThat(
            "Script is taken from fork",
            scripts,
            Matchers.contains(script)
        );
    }
}
