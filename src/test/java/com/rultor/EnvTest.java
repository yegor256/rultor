/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

/**
 * Test case for {@link Env}.
 *
 * @since 1.58
 */
@ExtendWith(SystemStubsExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
final class EnvTest {

    /**
     * Environment variables.
     */
    @SystemStub
    private EnvironmentVariables environment;

    @Test
    void readsFromManifest() {
        this.environment.remove(Env.SETTINGS_XML);
        MatcherAssert.assertThat(
            "takes the right value",
            Env.read("Rultor-SecurityKey"),
            Matchers.equalTo("${failsafe.security.key}")
        );
    }

    @Test
    void readsVersionFromManifest() {
        this.environment.remove(Env.SETTINGS_XML);
        MatcherAssert.assertThat(
            "takes the right value of the version",
            Env.read("Rultor-Version"),
            Matchers.not(Matchers.emptyString())
        );
    }

    @Test
    void readsRevisionFromManifest() {
        this.environment.remove(Env.SETTINGS_XML);
        MatcherAssert.assertThat(
            "takes the right value",
            Env.read("Rultor-Revision"),
            Matchers.not(Matchers.emptyString())
        );
    }

    @Test
    void readsFromSettingsXml() {
        this.environment.set(
            Env.SETTINGS_XML,
            String.join(
                "",
                "<settings><profiles><profile><properties>",
                "<test>hello</test>",
                "</properties></profile></profiles></settings>"
            )
        );
        MatcherAssert.assertThat(
            "has the right XML in env",
            System.getenv(Env.SETTINGS_XML),
            Matchers.not(Matchers.nullValue())
        );
        MatcherAssert.assertThat(
            "takes the right value",
            Env.read("Rultor-Test"),
            Matchers.equalTo("hello")
        );
    }

    @Test
    void readsRevisionFromSettingsXml() {
        this.environment.set(
            Env.SETTINGS_XML,
            "<settings/>"
        );
        MatcherAssert.assertThat(
            "takes the right value",
            Env.read("Rultor-Revision"),
            Matchers.not(Matchers.emptyString())
        );
    }

    @Test
    void readsVersionFromSettingsXml() {
        this.environment.set(
            Env.SETTINGS_XML,
            "<settings />"
        );
        MatcherAssert.assertThat(
            "takes the right value of the version, while it's absent in the XML",
            Env.read("Rultor-Version"),
            Matchers.not(Matchers.emptyString())
        );
    }
}
