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
