/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import com.jcabi.ssh.Ssh;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

/**
 * Shell in profile.
 *
 * <p>The profile is expected to carry all ssh entries; wrap it into
 * {@link SshProfile} to fill them in with defaults when missing.</p>
 *
 * @since 1.48
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "addr" })
public final class PfShell {

    /**
     * Profile with SSH settings.
     */
    private final transient Profile profile;

    /**
     * Host name or IP address to override the profile with, empty for none.
     */
    private final transient String addr;

    /**
     * Constructor.
     *
     * @param prof Profile with SSH settings
     */
    public PfShell(final Profile prof) {
        this(prof, "");
    }

    /**
     * Constructor.
     *
     * @param prof Profile with SSH settings
     * @param host Host name to override the profile with, empty for none
     */
    private PfShell(final Profile prof, final String host) {
        this.profile = prof;
        this.addr = host;
    }

    /**
     * Make a new shell, with a different address.
     *
     * @param host New address
     * @return New shell
     */
    public PfShell withHost(final String host) {
        return new PfShell(this.profile, host);
    }

    /**
     * Get host.
     *
     * @return Host name
     * @throws IOException If fails
     */
    public String host() throws IOException {
        final String host;
        if (this.addr.isEmpty()) {
            host = new Profile.Defaults(this.profile).text(
                "/p/entry[@key='ssh']/entry[@key='host']"
            );
        } else {
            host = this.addr;
        }
        return host;
    }

    /**
     * Get port.
     *
     * @return Port
     * @throws IOException If fails
     */
    public int port() throws IOException {
        return Integer.parseInt(
            new Profile.Defaults(this.profile).text(
                "/p/entry[@key='ssh']/entry[@key='port']"
            )
        );
    }

    /**
     * Get login.
     *
     * @return SSH login
     * @throws IOException If fails
     */
    public String login() throws IOException {
        return new Profile.Defaults(this.profile).text(
            "/p/entry[@key='ssh']/entry[@key='login']"
        );
    }

    /**
     * Get private key.
     *
     * <p>An entry injected by {@link SshDefaults} carries the default
     * key itself; a key configured by the profile is an asset reference
     * and must resolve to an existing asset.</p>
     *
     * @return Private SSH key
     * @throws IOException If fails
     */
    public String key() throws IOException {
        final String entry = "/p/entry[@key='ssh']/entry[@key='key']";
        final String key;
        if (this.profile.read()
            .nodes(String.format("%s[@default='true']", entry)).isEmpty()) {
            final String path = new Profile.Defaults(this.profile).text(
                entry, ""
            );
            if (this.profile.assets().get(path) == null) {
                throw new Profile.ConfigException(
                    String.format("Private SSH key not found at %s", path)
                );
            }
            try (InputStream stream = this.profile.assets().get(path)) {
                key = IOUtils.toString(
                    stream,
                    StandardCharsets.UTF_8
                );
            } catch (final IOException ex) {
                throw new Profile.ConfigException(ex);
            }
        } else {
            key = this.profile.read()
                .xpath(String.format("%s/text()", entry)).get(0);
        }
        return key;
    }

    /**
     * Make SSH shell.
     *
     * @return SSH shell
     * @throws IOException If fails
     */
    public Ssh toSsh() throws IOException {
        return new Ssh(this.host(), this.port(), this.login(), this.key());
    }
}
