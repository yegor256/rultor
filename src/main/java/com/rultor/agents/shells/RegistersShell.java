/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Registers shell.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "shell")
public final class RegistersShell extends AbstractAgent {

    /**
     * Shell in profile.
     */
    private final transient PfShell shell;

    /**
     * Constructor.
     *
     * @param profile Profile with SSH settings, e.g. {@link SshProfile}
     */
    public RegistersShell(final Profile profile) {
        this(new PfShell(profile));
    }

    /**
     * Constructor.
     *
     * @param pfshell Shell to register
     */
    private RegistersShell(final PfShell pfshell) {
        super(
            "/talk[daemon and not(shell)]",
            "/talk[not(ec2/instance)]"
        );
        this.shell = pfshell;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String hash = xml.xpath("/talk/daemon/@id").get(0);
        final Directives dirs = new Directives();
        try {
            Logger.info(
                this, "Shell %s registered as %s:%d in %s",
                hash, this.shell.host(), this.shell.port(),
                xml.xpath("/talk/@name").get(0)
            );
            final String host = this.shell.host();
            if (host.isEmpty()) {
                throw new Profile.ConfigException(
                    "SSH host is empty, it's a mistake"
                );
            }
            final String login = this.shell.login();
            if (login.isEmpty()) {
                throw new Profile.ConfigException(
                    "SSH login is empty, it's a mistake"
                );
            }
            final String key = this.shell.key();
            if (key.isEmpty()) {
                throw new Profile.ConfigException(
                    "SSH key is empty, it's a mistake"
                );
            }
            dirs.xpath("/talk").add("shell")
                .attr("id", hash)
                .add("host").set(host).up()
                .add("port").set(Integer.toString(this.shell.port())).up()
                .add("login").set(login).up()
                .add("key").set(key);
        } catch (final Profile.ConfigException ex) {
            dirs.xpath("/talk/daemon/script").set(
                String.format(
                    "Failed to read profile: %s", ex.getLocalizedMessage()
                )
            );
        }
        return dirs;
    }
}
