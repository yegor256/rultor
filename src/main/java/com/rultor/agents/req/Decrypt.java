/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import com.jcabi.aspects.Immutable;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Decrypt.
 *
 * @since 1.37.4
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "profile")
@SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
final class Decrypt {

    /**
     * Space delimiter.
     */
    private static final String SPACE = " ";

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param prof Profile
     */
    Decrypt(final Profile prof) {
        this.profile = prof;
    }

    /**
     * Decrypt instructions.
     * @return Instructions
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterable<String> commands() throws IOException {
        final Collection<XML> assets =
            this.profile.read().nodes("/p/entry[@key='decrypt']/entry");
        final Collection<String> commands = new LinkedList<>();
        for (final XML asset : assets) {
            final String key = asset.xpath("@key").get(0);
            final String enc = String.format("%s.enc", key);
            commands.add(
                String.join(
                    Decrypt.SPACE,
                    "gpg --verbose",
                    String.format(
                        "--decrypt %s > %s",
                        Ssh.escape(asset.xpath("./text()").get(0)),
                        Ssh.escape(enc)
                    )
                )
            );
            commands.add(
                String.format(
                    String.join(
                        Decrypt.SPACE,
                        "gpg --no-tty --batch --verbose --decrypt",
                        "--ignore-mdc-error",
                        "--passphrase %s %s > %s"
                    ),
                    Ssh.escape(
                        String.format("rultor-key:%s", this.profile.name())
                    ),
                    Ssh.escape(enc),
                    Ssh.escape(key)
                )
            );
            commands.add(String.format("rm -rf %s ", Ssh.escape(enc)));
        }
        return commands;
    }
}
