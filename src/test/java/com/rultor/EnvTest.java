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
