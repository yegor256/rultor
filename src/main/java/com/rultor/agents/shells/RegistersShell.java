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
import java.net.UnknownHostException;
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
@SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
public final class RegistersShell extends AbstractAgent {

    /**
     * Shell in profile.
     */
    private final transient PfShell shell;

    /**
     * Constructor.
     * @param profile Profile
     * @param host Default IP address or host name
     * @param port Default Port of server
     * @param user Default Login
     * @param key Default Private SSH key
     * @throws UnknownHostException in case of host is not resolved
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public RegistersShell(final Profile profile, final String host,
        final int port, final String user, final String key)
        throws UnknownHostException {
        super(
            "/talk[daemon and not(shell)]",
            "/talk[not(ec2/instance)]"
        );
        if (user.isEmpty()) {
            throw new IllegalArgumentException(
                "User name is mandatory"
            );
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException(
                "SSH key is mandatory"
            );
        }
        this.shell = new PfShell(
            profile,
            new SmartHost(host).ip(),
            port,
            user,
            key
        );
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
