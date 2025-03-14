/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import com.jcabi.aspects.Immutable;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.Env;
import com.rultor.agents.daemons.StartsDaemon;
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
     * Proxy usage command string.
     */
    private final transient String proxy;

    /**
     * Ctor.
     * @param prof Profile
     * @param host Host
     * @param port Port
     */
    Decrypt(final Profile prof, final String host, final int port) {
        this.profile = prof;
        if (host.isEmpty()) {
            this.proxy = "";
        } else {
            this.proxy = String.format("http-proxy=%s:%d", host, port);
        }
    }

    /**
     * Ctor.
     * @param prof Profile
     */
    Decrypt(final Profile prof) {
        this(prof, "", 0);
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
        if (!assets.isEmpty()) {
            commands.add("gpgconf --reload gpg-agent");
            commands.add(
                String.join(
                    Decrypt.SPACE,
                    "gpg --keyserver keyserver.ubuntu.com",
                    this.proxy,
                    String.format(
                        "--verbose --recv-keys %s",
                        Env.read("Rultor-GpgPublic")
                    )
                )
            );
            commands.add(
                String.join(
                    Decrypt.SPACE,
                    "gpg --import",
                    String.format(
                        "\"$(pwd)/%s/secring.gpg\"",
                        StartsDaemon.GPG_HOME
                    )
                )
            );
            commands.add("gpg --version");
            commands.add("gpg --list-keys");
            commands.add(
                String.format(
                    "ls -al \"$(pwd)/%s\"",
                    StartsDaemon.GPG_HOME
                )
            );
        }
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
        commands.add(String.format("rm -rf %s", StartsDaemon.GPG_HOME));
        if (!assets.isEmpty()) {
            commands.add(
                String.format(
                    "gpg --batch --yes --delete-secret-keys %s",
                    Env.read("Rultor-GpgSecret")
                )
            );
        }
        return commands;
    }
}
