/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.immutable.ArrayMap;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link PfShell}.
 *
 * @since 1.81
 */
final class PfShellTest {

    /**
     * PfShell can close asset stream after reading the private key.
     * @throws Exception In case of error.
     */
    @Test
    void closesKeyAssetStream() throws Exception {
        final String path = "id_rsa";
        final String key = "secret";
        final PfShellTest.ClosingStream stream =
            new PfShellTest.ClosingStream(key);
        final Profile profile = Mockito.mock(Profile.class);
        Mockito.doReturn(
            new XMLDocument(
                String.format(
                    "<p><entry key='ssh'><entry key='key'>%s</entry></entry></p>",
                    path
                )
            )
        ).when(profile).read();
        Mockito.doReturn(
            new ArrayMap<String, InputStream>().with(path, stream)
        ).when(profile).assets();
        MatcherAssert.assertThat(
            "Private key should be read from asset",
            new PfShell(profile, "localhost", 22, "rultor", "").key(),
            Matchers.equalTo(key)
        );
        MatcherAssert.assertThat(
            "Asset stream should be closed after reading",
            stream.isClosed(),
            Matchers.is(true)
        );
    }

    /**
     * Input stream that records closing.
     *
     * @since 1.81
     */
    private static final class ClosingStream extends ByteArrayInputStream {

        /**
         * Was it closed?
         */
        private transient boolean closed;

        /**
         * Ctor.
         * @param text Text to read
         */
        ClosingStream(final String text) {
            super(text.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void close() throws IOException {
            this.closed = true;
            super.close();
        }

        /**
         * Was it closed?
         * @return True if it was closed
         */
        boolean isClosed() {
            return this.closed;
        }
    }
}
