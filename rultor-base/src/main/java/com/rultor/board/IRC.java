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
import com.rultor.board.IRCServer.UtilFormatter;
import com.rultor.board.IRCServer.UtilLogger;
import javax.validation.constraints.NotNull;
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
@EqualsAndHashCode(of = { "host", "port", "channel" })
@Loggable(Loggable.DEBUG)
public final class IRC implements Billboard {
    /**
     * Channel name.
     * Or generally speaking:
     * target of PRIVMSGs (a channel or nickname)
     */
    private final transient String channel;

    /**
     * Creates connection and stores basic connection information.
     * Like host, port, channel.
     */
    private final transient IRCServer server;

    /**
     * Public ctor.
     *
     * @param hst Host
     * @param prt Port
     * @param chnl Channel
     */
    public IRC(final String hst, final int prt, final String chnl) {
        this(chnl, new IRCServer(hst, prt));
    }

    /**
     * Public ctor.
     *
     * @param chnl Channel
     * @param serverz Server
     */
    public IRC(final String chnl, final IRCServer serverz) {
        this.channel = chnl;
        this.server = serverz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void announce(
        @NotNull(message = "body can't be NULL") final String body) {
        final IRCConnection conn = this.server.connect(
            this.channel, "", "nickTest", "userTest", "nameTest", false
        );
        final String channelFormatted =
            UtilFormatter.formatChannelName(this.channel);
        conn.doPrivmsg(channelFormatted, body);
        UtilLogger.printFromProgram(
            String.format(
                "%s%s",
                UtilFormatter.formatChannelPrompt(channelFormatted), body
            )
        );
    }
}
