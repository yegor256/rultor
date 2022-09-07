/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
import com.rultor.spi.Profile;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.cactoos.text.Joined;
import org.cactoos.text.UncheckedText;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests for {@link Decrypt}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.37.4
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class DecryptTest {

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
     * Temp directory.
     * @checkstyle VisibilityModifierCheck (5 lines)
     */
    @Rule
    public final transient TemporaryFolder temp = new TemporaryFolder();

    /**
     * StartsRequest can take decryption instructions into account.
     * @throws Exception In case of error.
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void decryptsAssets() throws Exception {
        final Iterable<String> commands = new Decrypt(
            new Profile.Fixed(
                this.createTestProfileXML(),
                "test/test"
            )
        ).commands();
        final String script = new Joined(
            NEWLINE,
            "set -x",
            "set -e",
            "set -o pipefail",
            new Joined(
                NEWLINE,
                commands
            ).asString()
        ).asString();
        final File dir = this.temp.newFolder();
        FileUtils.write(
            new File(dir, "a.txt.asc"),
            new FakePGP().asString(),
            StandardCharsets.UTF_8
        );
        final String[] keys = {"secring"};
        for (final String key : keys) {
            final String gpg = IOUtils.toString(
                this.getClass().getResourceAsStream(
                    String.format("%s.gpg.base64", key)
                ),
                StandardCharsets.UTF_8
            );
            Assume.assumeThat(gpg, Matchers.not(Matchers.startsWith("${")));
            FileUtils.writeByteArrayToFile(
                new File(dir, String.format(".gnupg/%s.gpg", key)),
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
    public void testHttpProxyHandling() throws IOException {
        MatcherAssert.assertThat(
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
