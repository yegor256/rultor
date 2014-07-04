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
import com.rultor.agents.TalkAgent;
import com.rultor.spi.Talk;
import java.io.IOException;
import org.xembly.Directives;

/**
 * Registers shell.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class RegistersShell implements TalkAgent {

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
     * @param adr IP address
     * @param prt Port of server
     * @param user Login
     * @param priv Private SSH key
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public RegistersShell(final String adr, final int prt,
        final String user, final String priv) {
        this.addr = adr;
        this.login = user;
        this.key = priv;
        this.port = prt;
    }


    @Override
    public void execute(final Talk talk) throws IOException {
        final XML xml = talk.read();
        if (xml.nodes("/talk/shell").isEmpty()) {
            talk.modify(
                new Directives()
                    .xpath("/talk[not(shell)]").strict(1).add("shell")
                    .add("host").set(this.addr).up()
                    .add("port").set(Integer.toString(this.port)).up()
                    .add("login").set(this.login).up()
                    .add("key").set(this.key),
                String.format("shell registered as %s:%d", this.addr, this.port)
            );
            Logger.info(
                this, "shell %s:%d registered at %s",
                this.addr, this.port, talk
            );
        } else {
            Logger.info(
                this, "shell already registered at %s: %s",
                talk, xml.xpath("/talk/shell/host/text()").get(0)
            );
        }
    }
}
