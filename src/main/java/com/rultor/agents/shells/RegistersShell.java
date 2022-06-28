/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Registers shell.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
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
     * @param profile Profile
     * @param host Default IP address
     * @param port Default Port of server
     * @param user Default Login
     * @param key Default Private SSH key
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public RegistersShell(final Profile profile, final String host,
        final int port, final String user, final String key) {
        super("/talk[daemon and not(shell)]");
        this.shell = new PfShell(profile, host, port, user, key);
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String hash = xml.xpath("/talk/daemon/@id").get(0);
        final Directives dirs = new Directives();
        try {
            Logger.info(
                this, "shell %s registered as %s:%d in %s",
                hash, this.shell.host(), this.shell.port(),
                xml.xpath("/talk/@name").get(0)
            );
            dirs.xpath("/talk").add("shell")
                .attr("id", hash)
                .add("host").set(this.shell.host()).up()
                .add("port").set(Integer.toString(this.shell.port())).up()
                .add("login").set(this.shell.login()).up()
                .add("key").set(this.shell.key());
        } catch (final Profile.ConfigException ex) {
            dirs.xpath("/talk/daemon/script").set(
                String.format(
                    "failed to read profile: %s", ex.getLocalizedMessage()
                )
            );
        }
        return dirs;
    }
}
