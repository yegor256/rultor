/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
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
 * @since 1.37.4
 */
final class DecryptTest {

    /**
     * Newline.
     */
    private static final String NEWLINE = System.lineSeparator();

    /**
     * StartsRequest can take decryption instructions into account.
     * @param temp Temporary folder
     * @throws Exception In case of error.
     */
    @Test
    void keepsThePassphraseOutOfTheTrace() throws Exception {
        final String script = new Joined(
            DecryptTest.NEWLINE,
            new Decrypt(
                new Profile.Fixed(this.createTestProfileXml(), "test/test")
            ).commands()
        ).asString();
        MatcherAssert.assertThat(
            "The passphrase must not stand on a command line the shell traces",
            script,
            Matchers.not(Matchers.containsString("--passphrase '"))
        );
        MatcherAssert.assertThat(
            "The tracing must be off while the passphrase is piped in",
            script,
            Matchers.containsString("set +x")
        );
    }

    @Test
    void decryptsAssets(@TempDir final Path temp) throws Exception {
        final String script = new Joined(
            DecryptTest.NEWLINE,
            "set -ex -o pipefail",
            new Joined(
                DecryptTest.NEWLINE,
                new Decrypt(
                    new Profile.Fixed(
                        this.createTestProfileXml(),
                        "test/test"
                    )
                ).commands()
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
        try (
            VerboseProcess proc = new VerboseProcess(
                new ProcessBuilder()
                    .command("/bin/bash", "-c", script)
                    .directory(dir)
                    .redirectErrorStream(true),
                Level.WARNING, Level.WARNING
            )
        ) {
            proc.stdout();
        }
        MatcherAssert.assertThat(
            "File should be decrypted",
            FileUtils.readFileToString(
                new File(dir, "a.txt"),
                StandardCharsets.UTF_8
            ),
            Matchers.startsWith("hello, world!")
        );
    }

    private XMLDocument createTestProfileXml() {
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
