/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for ${@link PfShell}.
 *
 * @since 2.1
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
final class PfShellTest {

    /**
     * PfShell can resolve the host from the profile.
     *
     * @throws Exception In case of error.
     */
    @Test
    void resolvesHostFromProfile() throws Exception {
        MatcherAssert.assertThat(
            "Host should come from the profile",
            PfShellTest.shell().host(),
            Matchers.equalTo("192.168.0.7")
        );
    }

    /**
     * PfShell can resolve the port from the profile.
     *
     * @throws Exception In case of error.
     */
    @Test
    void resolvesPortFromProfile() throws Exception {
        MatcherAssert.assertThat(
            "Port should come from the profile",
            PfShellTest.shell().port(),
            Matchers.equalTo(2222)
        );
    }

    /**
     * PfShell can resolve the login from the profile.
     *
     * @throws Exception In case of error.
     */
    @Test
    void resolvesLoginFromProfile() throws Exception {
        MatcherAssert.assertThat(
            "Login should come from the profile",
            PfShellTest.shell().login(),
            Matchers.equalTo("bob")
        );
    }

    /**
     * PfShell can override the host with {@link PfShell#withHost(String)}.
     *
     * @throws Exception In case of error.
     */
    @Test
    void overridesHost() throws Exception {
        MatcherAssert.assertThat(
            "Overridden host should win",
            new PfShell(
                new Profile.Fixed(
                    "<p><entry key='ssh'>",
                    "<entry key='host'>192.168.0.7</entry>",
                    "</entry></p>"
                )
            ).withHost("10.0.0.1").host(),
            Matchers.equalTo("10.0.0.1")
        );
    }

    /**
     * PfShell can resolve the private key from an asset.
     *
     * @throws Exception In case of error.
     */
    @Test
    void resolvesKeyFromAsset() throws Exception {
        final Profile profile = Mockito.mock(Profile.class);
        Mockito.doReturn(
            new XMLDocument(
                "<p><entry key='ssh'><entry key='key'>key.pem</entry></entry></p>"
            )
        ).when(profile).read();
        Mockito.doReturn(
            Collections.singletonMap(
                "key.pem",
                new ByteArrayInputStream(
                    "asset-key".getBytes(StandardCharsets.UTF_8)
                )
            )
        ).when(profile).assets();
        MatcherAssert.assertThat(
            "Key should be loaded from the asset",
            new PfShell(profile).key(),
            Matchers.equalTo("asset-key")
        );
    }

    /**
     * PfShell can use the key entry value when there is no such asset.
     *
     * @throws Exception In case of error.
     */
    @Test
    void usesKeyValueWhenAssetAbsent() throws Exception {
        MatcherAssert.assertThat(
            "Key entry value should be used when the asset is absent",
            new PfShell(
                new Profile.Fixed(
                    "<p><entry key='ssh'>",
                    "<entry key='key'>inline-key</entry>",
                    "</entry></p>"
                )
            ).key(),
            Matchers.equalTo("inline-key")
        );
    }

    private static PfShell shell() {
        return new PfShell(
            new Profile.Fixed(
                "<p><entry key='ssh'>",
                "<entry key='host'>192.168.0.7</entry>",
                "<entry key='port'>2222</entry>",
                "<entry key='login'>bob</entry>",
                "</entry></p>"
            )
        );
    }
}
