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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
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
            "set -ex -o pipefail",
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
        final String secring = System.getenv("GPG_SECRING");
        Assumptions.assumeFalse(secring == null);
        Assumptions.assumeTrue(secring.startsWith("---"));
        FileUtils.writeByteArrayToFile(
            new File(
                dir,
                String.format("%s/secring.gpg.asc", StartsDaemon.GPG_HOME)
            ),
            secring.getBytes(StandardCharsets.UTF_8)
        );
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
