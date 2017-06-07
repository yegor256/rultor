/**
 * Copyright (c) 2009-2017, rultor.com
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

import com.google.common.base.Joiner;
import com.jcabi.log.VerboseProcess;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
 */
public final class DecryptTest {

    /**
     * Encoded test file.
     */
    private static final String ASC = Joiner.on('\n').join(
        "-----BEGIN PGP MESSAGE-----",
        "Version: GnuPG v1\n",
        "hQEMA5qETcGag5w6AQgAvm/P0JUlQAdOtGng5zHLx5cV+BrbpFt1m2ja4BjacYMU",
        "wcubtJSh+n0XNLk6zMMCsrDnTfzvi/FEFaRsPVb/ZJHiJGvwhNGyenQWgd6bczIL",
        "1UxBZ1BpHTPv5hVK43fb6cYq+e/gniBMvIKlKV+Qh/NVtiQACQJ5xL1M16S9SQuY",
        "hjnVEL3JNHiLEAfPS/8xS05DY/w1k/JyPXMZlrR7YGMxUsG6aDaFPAdjcdSbzGCT",
        "j4yZPdZtyqePFGXn0VJE7GRywWcmk3Nj+oZzgx6DLV3PH40HSYNuyA9a2xFpghTr",
        "7uiYRf+rRzXlx7qnBLsvETlhc77zpf0FW4pLq/08ttLADQFsIU2BNHJGPw+96GKJ",
        "AVNAm0OxfaMz+U+gy2kIgteuMQmfkYDF0u9HE7NwZ1PlXO5Oszhfdim2LPSyxYMi",
        "sKlVilWhPwdumSjmY0IG1B6yc8ZLG4BjBucu3dMjj98iKRjlKvEmqqdUmoZY+l/N",
        "Ye9gRf0UY44jJ0f4H81osGtmXg1dRc47OE/pUGGbIare4GNvBB/oiksvoCDOOEKy",
        "cj6IAjR/BnSZ1mYvSShSPatu7QRFdd/HFRt76pGj2G6ibnnDNpfjDwgNaWbiGUU=",
        "=d2bb",
        "-----END PGP MESSAGE-----"
    );

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
        final String script = Joiner.on('\n').join(
            "set -x",
            "set -e",
            "set -o pipefail",
            Joiner.on('\n').join(commands)
        );
        final File dir = this.temp.newFolder();
        FileUtils.write(new File(dir, "a.txt.asc"), DecryptTest.ASC);
        final String[] keys = {"pubring", "secring"};
        for (final String key : keys) {
            final String gpg = IOUtils.toString(
                this.getClass().getResourceAsStream(
                    String.format("%s.gpg.base64", key)
                )
            );
            Assume.assumeThat(gpg, Matchers.not(Matchers.startsWith("${")));
            FileUtils.writeByteArrayToFile(
                new File(dir, String.format(".gpg/%s.gpg", key)),
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
            FileUtils.readFileToString(new File(dir, "a.txt")),
            Matchers.startsWith("hello, world!")
        );
    }

    /**
     * This test reproduces issue #635 and validates that Decrypt uses HTTP
     * proxy server settings when running gpg, if they are provided.
     * @throws java.io.IOException Thrown in case of XML parsing error
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
            Joiner.on("").join(
                "<p>",
                "<entry key='decrypt'>",
                "<entry key='a.txt'>a.txt.asc</entry>",
                "</entry>",
                "</p>"
            )
        );
    }
}
