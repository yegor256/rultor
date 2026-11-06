/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.matchers.XhtmlMatchers;
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
 * Tests for ${@link SshProfile}.
 *
 * @since 2.1
 */
final class SshProfileTest {

    /**
     * SshProfile can fill SSH entries when they are absent.
     *
     * @throws Exception In case of error.
     */
    @Test
    void fillsSshEntriesWhenAbsent() throws Exception {
        MatcherAssert.assertThat(
            "SSH entries should be filled in with defaults",
            new SshProfile(
                new Profile.Fixed("<p><entry key='merge'/></p>"),
                new SshDefaults("b4.rultor.com", 22, "rultor", "secret-key")
            ).read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='merge']",
                "/p/entry[@key='ssh']/entry[@key='host'][.='b4.rultor.com']",
                "/p/entry[@key='ssh']/entry[@key='port'][.='22']",
                "/p/entry[@key='ssh']/entry[@key='login'][.='rultor']",
                "/p/entry[@key='ssh']/entry[@key='key'][.='secret-key']"
            )
        );
    }

    /**
     * SshProfile can keep the SSH entries already present.
     *
     * @throws Exception In case of error.
     */
    @Test
    void keepsPresentEntries() throws Exception {
        MatcherAssert.assertThat(
            "Present SSH entries should not be overridden",
            new SshProfile(
                new Profile.Fixed(
                    "<p><entry key='ssh'>",
                    "<entry key='host'>mine.example.com</entry>",
                    "<entry key='login'>bob</entry>",
                    "</entry></p>"
                ),
                new SshDefaults("b4.rultor.com", 22, "rultor", "secret-key")
            ).read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='ssh']/entry[@key='host'][.='mine.example.com']",
                "/p/entry[@key='ssh']/entry[@key='login'][.='bob']",
                "/p/entry[@key='ssh']/entry[@key='port'][.='22']",
                "/p/entry[@key='ssh']/entry[@key='key'][.='secret-key']"
            )
        );
    }

    /**
     * SshProfile can delegate the profile name.
     */
    @Test
    void delegatesProfileName() {
        MatcherAssert.assertThat(
            "Name should be delegated",
            new SshProfile(
                new Profile.Fixed(
                    new XMLDocument("<p><entry key='merge'/></p>"),
                    "test/test",
                    "main"
                ),
                new SshDefaults("b4.rultor.com", 22, "rultor", "secret-key")
            ).name(),
            Matchers.equalTo("test/test")
        );
    }

    /**
     * SshProfile can delegate the default branch.
     */
    @Test
    void delegatesDefaultBranch() {
        MatcherAssert.assertThat(
            "Branch should be delegated",
            new SshProfile(
                new Profile.Fixed(
                    new XMLDocument("<p><entry key='merge'/></p>"),
                    "test/test",
                    "main"
                ),
                new SshDefaults("b4.rultor.com", 22, "rultor", "secret-key")
            ).defaultBranch(),
            Matchers.equalTo("main")
        );
    }

    /**
     * SshProfile can delegate assets.
     *
     * @throws Exception In case of error.
     */
    @Test
    void delegatesAssets() throws Exception {
        final Profile origin = Mockito.mock(Profile.class);
        Mockito.doReturn(
            Collections.singletonMap(
                "key.pem",
                new ByteArrayInputStream(
                    "the-key".getBytes(StandardCharsets.UTF_8)
                )
            )
        ).when(origin).assets();
        MatcherAssert.assertThat(
            "Assets should be delegated",
            new SshProfile(
                origin, new SshDefaults("b4.rultor.com", 22, "rultor", "k")
            ).assets(),
            Matchers.hasKey("key.pem")
        );
    }
}
