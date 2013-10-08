/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.board;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.schwering.irc.lib.IRCConnection;

/**
 * The IRC client API implementation.
 *
 * @see <a href="http://moepii.sourceforge.net/">IRClib</a>
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "channel", "nickname", "username" })
@Loggable(Loggable.DEBUG)
public final class IRC implements Billboard {
    /**
     * Channel name.
     * Or generally speaking:
     * target of PRIVMSGs (a channel or nickname)
     */
    private final transient String channel;

    /**
     * Password.
     */
    private final transient String password;

    /**
     * Nickname.
     */
    private final transient String nickname;

    /**
     * Username.
     */
    private final transient String username;

    /**
     * Real name.
     */
    private final transient String realname;

    /**
     * Is SSL used.
     */
    private final transient boolean isSSL;

    /**
     * Bill to publish.
     */
    private final transient Bill bill;

    /**
     * Creates connection and stores basic connection information.
     * Like host, port, channel.
     */
    private final transient IRCServer server;

    /**
     * Public ctor.
     *
     * @param bll Bill
     * @param hst Host
     * @param prt Port
     * @param chnl Channel
     * @param pass Password
     * @param nick Nickname
     * @param user Username
     * @param name Real name
     * @param ssl Is SSL used
     * @checkstyle ParameterNumber (3 lines)
     */
    public IRC(final Bill bll, final String hst, final int prt,
        final String chnl, final String pass, final String nick,
        final String user, final String name, final boolean ssl) {
        this(bll, new IRCServerDefault(hst, prt), chnl, pass, nick, user, name,
            ssl
        );
    }

    /**
     * Public ctor.
     *
     * @param bll Bill
     * @param srv Server
     * @param chnl Channel
     * @param pass Password
     * @param nick Nickname
     * @param user Username
     * @param name Real name
     * @param ssl Is SSL used
     * @checkstyle ParameterNumber (3 lines)
     */
    public IRC(final Bill bll, final IRCServer srv, final String chnl,
        final String pass, final String nick, final String user,
        final String name, final boolean ssl
    ) {
        this.bill = bll;
        this.server = srv;
        this.channel = chnl;
        this.password = pass;
        this.nickname = nick;
        this.username = user;
        this.realname = name;
        this.isSSL = ssl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void announce(final boolean success) {
        final IRCConnection conn;
        try {
            conn = this.server.connect(
                this.channel, this.password, this.nickname, this.username,
                this.realname, this.isSSL
            );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        final String formatted =
            IRCServerDefault.formatChannelName(this.channel);
        conn.doPrivmsg(formatted, this.bill.subject());
        Logger.info(
            this, "%s%s",
            IRCServerDefault.formatChannelPrompt(formatted), this.bill.subject()
        );
    }
}
