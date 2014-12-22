/**
 * Copyright (c) 2009-2014, rultor.com
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
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Registers shell.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "addr", "port", "login", "key" })
public final class RegistersShell extends AbstractAgent {

    /**
     * IP address of the server.
     */
    private final transient String addr;

    /**
     * Port to use.
     */
    private final transient int port;

    /**
     * User name.
     */
    private final transient String login;

    /**
     * Private SSH key.
     */
    private final transient String key;

    /**
     * Constructor.
     * @param profile Profile
     * @param adr Default IP address
     * @param prt Default Port of server
     * @param user Defaul Login
     * @param priv Default Private SSH key
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public RegistersShell(final Profile profile, final String adr,
        final int prt, final String user, final String priv) {
        super("/talk[daemon and not(shell)]");
        try {
            final Profile.Defaults prof = new Profile.Defaults(profile);
            this.addr = prof.text(
                "/p/entry[@key='ssh']/entry[@key='host']", adr
            );
            this.port = Integer.parseInt(
                prof.text(
                    "/p/entry[@key='ssh']/entry[@key='port']",
                    Integer.toString(prt)
                )
            );
            this.login = prof.text(
                "/p/entry[@key='ssh']/entry[@key='login']", user
            );
            final String keypath = prof.text(
                "/p/entry[@key='ssh']/entry[@key='key']", ""
            );
            if (keypath.isEmpty()) {
                this.key = priv;
            } else {
                this.key = IOUtils.toString(
                    this.getClass().getResourceAsStream(keypath),
                    CharEncoding.UTF_8
                );
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Iterable<Directive> process(final XML xml) {
        final String hash = xml.xpath("/talk/daemon/@id").get(0);
        Logger.info(
            this, "shell %s registered as %s:%d in %s",
            hash, this.addr, this.port, xml.xpath("/talk/@name").get(0)
        );
        return new Directives()
            .xpath("/talk").add("shell")
            .attr("id", hash)
            .add("host").set(this.addr).up()
            .add("port").set(Integer.toString(this.port)).up()
            .add("login").set(this.login).up()
            .add("key").set(this.key);
    }
}
