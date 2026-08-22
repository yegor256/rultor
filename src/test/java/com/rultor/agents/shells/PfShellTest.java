/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.immutable.ArrayMap;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link PfShell}.
 *
 * @since 1.77
 */
final class PfShellTest {

    /**
     * PfShell closes private key asset stream.
     * @throws Exception If something goes wrong.
     */
    @Test
    void closesPrivateKeyAssetStream() throws Exception {
        final String path = "id_rsa";
        final String pvt = "private-key";
        final PfShellTest.CloseAwareInputStream stream =
            new PfShellTest.CloseAwareInputStream(pvt);
        final Profile profile = Mockito.mock(Profile.class);
        Mockito.doReturn(
            new XMLDocument(
                String.join(
                    "",
                    "<p><entry key='ssh'><entry key='key'>",
                    path,
                    "</entry></entry></p>"
                )
            )
        ).when(profile).read();
        Mockito.doReturn(
            new ArrayMap<String, InputStream>().with(path, stream)
        ).when(profile).assets();
        MatcherAssert.assertThat(
            "Private key should be loaded from the profile asset",
            new PfShell(profile, "localhost", 22, "rultor", "fallback").key(),
            Matchers.equalTo(pvt)
        );
        MatcherAssert.assertThat(
            "Private key asset stream should be closed after reading",
            stream.closed(),
            Matchers.is(true)
        );
    }

    /**
     * Input stream with close state.
     *
     * @since 1.77
     */
    private static final class CloseAwareInputStream
        extends ByteArrayInputStream {

        /**
         * Whether close was called.
         */
        private transient boolean done;

        /**
         * Ctor.
         * @param body Stream body
         */
        CloseAwareInputStream(final String body) {
            super(body.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() {
            this.done = true;
        }

        /**
         * Check whether close was called.
         * @return True if closed.
         */
        boolean closed() {
            return this.done;
        }
    }
}
