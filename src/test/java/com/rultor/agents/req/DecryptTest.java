/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import com.jcabi.log.VerboseProcess;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.daemons.StartsDaemon;
import com.rultor.spi.Profile;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cactoos.text.Joined;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for {@link Decrypt}.
 *
 * @since 1.37.4
 */
final class DecryptTest {
    /**
     * Newline.
     */
    private static final String NEWLINE = "\n";

    /**
     * Test port for proxy settings test.
     * @see #testHttpProxyHandling()
     */
    private static final int PORT = 8080;

    /**
     * StartsRequest can take decryption instructions into account.
     * @param temp Temporary folder
     * @throws Exception In case of error.
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void decryptsAssets(@TempDir final Path temp) throws Exception {
        final Iterable<String> commands = new Decrypt(
            new Profile.Fixed(
                this.createTestProfileXML(),
                "test/test"
            )
        ).commands();
        final String script = new Joined(
            DecryptTest.NEWLINE,
            "set -x",
            "set -e",
            "set -o pipefail",
            new Joined(
                DecryptTest.NEWLINE,
                commands
            ).asString()
        ).asString();
        final File dir = temp.toFile();
        FileUtils.write(
            new File(dir, "a.txt.asc"),
            new FakePGP().asString(),
            StandardCharsets.UTF_8
        );
        final String[] keys = {"secring"};
        for (final String key : keys) {
            final String gpg = IOUtils.toString(
                this.getClass().getResource(
                    String.format("%s.gpg.base64", key)
                ),
                StandardCharsets.UTF_8
            );
            Assumptions.assumeFalse(gpg.startsWith("${"));
            FileUtils.writeByteArrayToFile(
                new File(
                    dir,
                    String.format("%s/%s.gpg", StartsDaemon.GPG_HOME, key)
                ),
                Base64.decodeBase64(gpg)
            );
        }
        new VerboseProcess(
            new ProcessBuilder().command(
                "/bin/bash", "-c", script
            ).directory(dir).redirectErrorStream(true),
            Level.WARNING, Level.WARNING
        ).stdout();
        MatcherAssert.assertThat(
            "File should be decrypted",
            FileUtils.readFileToString(
                new File(dir, "a.txt"),
                StandardCharsets.UTF_8
            ),
            Matchers.startsWith("hello, world!")
        );
    }

    /**
     * This test reproduces issue #635 and validates that Decrypt uses HTTP
     * proxy server settings when running gpg, if they are provided.
     * @throws IOException Thrown in case of XML parsing error
     */
    @Test
    void testHttpProxyHandling() throws IOException {
        MatcherAssert.assertThat(
            "proxy should be added to commands",
            new Decrypt(
                new Profile.Fixed(
                    this.createTestProfileXML(),
                    "test1/test1"
                ),
                "http://someserver.com",
                DecryptTest.PORT
            ).commands(),
            Matchers.hasItem(
                Matchers.containsString(
                    " http-proxy=http://someserver.com:8080 "
                )
            )
        );
    }

    /**
     * Creates a profile XML for testing purposes.
     *
     * @return XML document
     * @checkstyle AbbreviationAsWordInNameCheck (15 lines)
     */
    private XMLDocument createTestProfileXML() {
        return new XMLDocument(
            new UncheckedText(
                new Joined(
                    "",
                    "<p>",
                    "<entry key='decrypt'>",
                    "<entry key='a.txt'>a.txt.asc</entry>",
                    "</entry>",
                    "</p>"
                )
            ).asString()
        );
    }
}
