/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import com.jcabi.ssh.Ssh;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

/**
 * Shell in profile.
 *
 * @since 1.48
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "addr", "prt", "user", "pvt" })
public final class PfShell {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Host name or IP address.
     */
    private final transient String addr;

    /**
     * Port to use.
     */
    private final transient int prt;

    /**
     * User name.
     */
    private final transient String user;

    /**
     * Private SSH key.
     */
    private final transient String pvt;

    /**
     * Constructor.
     * @param prof Profile
     * @param host Default IP address or Host name
     * @param port Default Port of server
     * @param login Default Login
     * @param key Default Private SSH key
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public PfShell(final Profile prof, final String host,
        final int port, final String login, final String key) {
        this.profile = prof;
        this.addr = host;
        this.prt = port;
        this.user = login;
        this.pvt = key;
    }

    /**
     * Make a new shell, with a different address.
     * @param host New address
     * @return New shell
     */
    public PfShell withHost(final String host) {
        return new PfShell(this.profile, host, this.prt, this.user, this.pvt);
    }

    /**
     * Get host.
     * @return Host name
     * @throws IOException If fails
     */
    public String host() throws IOException {
        return new Profile.Defaults(this.profile).text(
            "/p/entry[@key='ssh']/entry[@key='host']", this.addr
        );
    }

    /**
     * Get port.
     * @return Port
     * @throws IOException If fails
     */
    public int port() throws IOException {
        return Integer.parseInt(
            new Profile.Defaults(this.profile).text(
                "/p/entry[@key='ssh']/entry[@key='port']",
                Integer.toString(this.prt)
            )
        );
    }

    /**
     * Get login.
     * @return SSH login
     * @throws IOException If fails
     */
    public String login() throws IOException {
        return new Profile.Defaults(this.profile).text(
            "/p/entry[@key='ssh']/entry[@key='login']", this.user
        );
    }

    /**
     * Get private key.
     * @return Private SSH key
     * @throws IOException If fails
     */
    public String key() throws IOException {
        final String path = new Profile.Defaults(this.profile).text(
            "/p/entry[@key='ssh']/entry[@key='key']", ""
        );
        final String key;
        if (path.isEmpty()) {
            key = this.pvt;
        } else {
            if (this.profile.assets().get(path) == null) {
                throw new Profile.ConfigException(
                    String.format("Private SSH key not found at %s", path)
                );
            }
            try {
                key = IOUtils.toString(
                    this.profile.assets().get(path),
                    StandardCharsets.UTF_8
                );
            } catch (final IOException ex) {
                throw new Profile.ConfigException(ex);
            }
        }
        return key;
    }

    /**
     * Make SSH shell.
     * @return SSH shell
     * @throws UnknownHostException If fails
     */
    public Ssh toSsh() throws UnknownHostException {
        return new Ssh(this.addr, this.prt, this.user, this.pvt);
    }

}
