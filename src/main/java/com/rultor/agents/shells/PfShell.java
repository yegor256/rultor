/**
 * Copyright (c) 2009-2015, rultor.com
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
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import com.rultor.spi.Profile;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

/**
 * Shell in profile.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.48
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "addr", "prt", "user", "pvt" })
final class PfShell {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Host name.
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
     * @param host Default IP address
     * @param port Default Port of server
     * @param login Defaul Login
     * @param key Default Private SSH key
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    PfShell(final Profile prof, final String host,
        final int port, final String login, final String key) {
        this.profile = prof;
        this.addr = host;
        this.prt = port;
        this.user = login;
        this.pvt = key;
    }

    /**
     * Get host.
     * @return Host name
     * @throws Profile.ConfigException If fails
     */
    public String host() throws Profile.ConfigException {
        return new Profile.Defaults(this.profile).text(
            "/p/entry[@key='ssh']/entry[@key='host']", this.addr
        );
    }

    /**
     * Get port.
     * @return Port
     * @throws Profile.ConfigException If fails
     */
    public int port() throws Profile.ConfigException {
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
     * @throws Profile.ConfigException If fails
     */
    public String login() throws Profile.ConfigException {
        return new Profile.Defaults(this.profile).text(
            "/p/entry[@key='ssh']/entry[@key='login']", this.user
        );
    }

    /**
     * Get private key.
     * @return Private SSH key
     * @throws Profile.ConfigException If fails
     */
    public String key() throws Profile.ConfigException {
        final String path = new Profile.Defaults(this.profile).text(
            "/p/entry[@key='ssh']/entry[@key='key']", ""
        );
        final String key;
        if (path.isEmpty()) {
            key = this.pvt;
        } else {
            try {
                key = IOUtils.toString(this.profile.assets().get(path));
            } catch (final IOException ex) {
                throw new Profile.ConfigException(ex);
            }
        }
        return key;
    }

}
