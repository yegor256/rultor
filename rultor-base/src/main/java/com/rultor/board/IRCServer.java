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
import java.security.cert.X509Certificate;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLIRCConnection;
import org.schwering.irc.lib.ssl.SSLTrustManager;

/**
 * Actually a factory of an IRCConnection connection.
 *
 * @author Konstantin Voytenko (cppruler@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "host", "port" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class IRCServer implements IRCServerInterface {
    /**
     * Host name.
     */
    private final transient String host;

    /**
     * Port number.
     */
    private final transient int port;

    /**
     * Public ctor.
     *
     * @param hst Host
     * @param prt Port
     */
    public IRCServer(final String hst, final int prt) {
        this.host = hst;
        this.port = prt;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (4 lines)
     */
    @Override
    public IRCConnection connect(final String channel,
        final String pass, final String nick,
        final String user, final String name, final boolean ssl)
        throws IOException {
        final IRCConnection conn = this.connection(
            this.host, this.port, pass, nick, user, name, ssl
        );
        this.connectToServerAndJoinChannel(conn, channel);
        return conn;
    }

    /**
     * Connects to the server and joins the channel.
     *
     * @param conn Connection
     * @param channel Channel
     * @throws IOException Exception during connecting to server
     */
    private void connectToServerAndJoinChannel(final IRCConnection conn,
        final String channel) throws IOException {
        final Waiter waiterConnected = new Waiter();
        final Waiter waiterJoined = new Waiter();
        this.connSetOptions(conn, waiterConnected, waiterJoined);
        conn.connect();
        waiterConnected.sleepUntilHappenned();
        this.joinChannel(conn, channel);
        waiterJoined.sleepUntilHappenned();
    }

    /**
     * Instantiates a connection object to talk to IRC server.
     *
     * @param hst Host
     * @param prt Port
     * @param pass Password
     * @param nick Nickname
     * @param user Username
     * @param name User real name
     * @param ssl Is SSL
     * @return IRCConnection
     * @checkstyle ParameterNumber (4 lines)
     */
    private IRCConnection connection(final String hst,
        final int prt, final String pass, final String nick,
        final String user, final String name, final boolean ssl) {
        IRCConnection connct;
        if (ssl) {
            connct = new SSLIRCConnection(
                hst, new int[]{prt}, pass, nick, user, name
            );
            ((SSLIRCConnection) connct).addTrustManager(new TrustManager());
        } else {
            connct = new IRCConnection(
                hst, new int[]{prt}, pass, nick, user, name
            );
        }
        return connct;
    }

    /**
     * Setups common IRC protocol options.
     *
     * @param conn Connection
     * @param connected Waiter until connected to server
     * @param joined Waiter until joined the server
     */
    private void connSetOptions(final IRCConnection conn,
        final Waiter connected, final Waiter joined) {
        conn.addIRCEventListener(
            new AbstractListener() {
                @Override
                public void onRegistered() {
                    connected.triggerHappenned();
                }
                @Override
                public void onJoin(final String chan, final IRCUser user) {
                    Logger.info(
                        this, "%s%s joins",
                        IRCServer.formatChannelPrompt(chan), user.getNick()
                    );
                    joined.triggerHappenned();
                }
            }
        );
        conn.setEncoding("UTF-8");
        conn.setPong(true);
        conn.setDaemon(false);
        conn.setColors(false);
    }

    /**
     * Join the channel.
     * @param conn Connection
     * @param channelz Channel name
     */
    private void joinChannel(final IRCConnection conn,
        final String channelz) {
        final String command = String.format(
            "join %s", IRCServer.formatChannelName(channelz)
        );
        Logger.info(this, command);
        conn.send(command);
    }

    /**
     * Treats IRC events. The most of them are just printed.
     */
    @Immutable
    @ToString
    abstract static class AbstractListener implements IRCEventListener {

        /**
         * Event - On Registered.
         */
        public abstract void onRegistered();

        /**
         * Event - On disconnected.
         */
        public final void onDisconnected() {
            Logger.info(this, "Disconnected");
        }

        /**
         * Event - On error.
         * @param msg Error message
         */
        public final void onError(final String msg) {
            Logger.info(this, "Error: %s", msg);
        }

        /**
         * Event - On error.
         * @param num Error num
         * @param msg Error message
         */
        public final void onError(final int num, final String msg) {
            Logger.info(this, "Error #%d: %s", num, msg);
        }

        /**
         * Event - On invitation.
         * @param chan Channel name
         * @param user User
         * @param nickpass Password
         */
        public final void onInvite(final String chan, final IRCUser user,
            final String nickpass) {
            Logger.info(
                this, "%s%s invites %s", IRCServer.formatChannelPrompt(chan),
                user.getNick(), nickpass
            );
        }

        /**
         * Event - On Join.
         * @param chan Channel name
         * @param user User
         */
        public abstract void onJoin(final String chan, final IRCUser user);

        /**
         * Event - On Kick.
         * @param chan Channel name
         * @param user User
         * @param nickpass Password
         * @param msg Message
         * @checkstyle ParameterNumber (2 lines)
         */
        public final void onKick(final String chan, final IRCUser user,
            final String nickpass,
            final String msg) {
            Logger.info(
                this, "%s%s kicks %s", IRCServer.formatChannelPrompt(chan),
                user.getNick(), nickpass
            );
        }

        /**
         * Event - On Mode changed.
         * @param user User
         * @param nickpass Password
         * @param mode Mode
         */
        public final void onMode(final IRCUser user, final String nickpass,
            final String mode) {
            Logger.info(
                this, "Mode: %s sets modes %s %s", user.getNick(), mode,
                nickpass
            );
        }

        /**
         * Event - On Mode changed.
         * @param chan Channel name
         * @param user User
         * @param modeparser Mode Parser
         */
        public final void onMode(final String chan, final IRCUser user,
            final IRCModeParser modeparser) {
            Logger.info(
                this, "%s%s sets mode: %s",
                IRCServer.formatChannelPrompt(chan), user.getNick(),
                modeparser.getLine()
            );
        }

        /**
         * Event - On nickname change.
         * @param user User
         * @param nicknew New nickname
         */
        public final void onNick(final IRCUser user, final String nicknew) {
            Logger.info(
                this, "Nick: %s is now known as %s", user.getNick(), nicknew
            );
        }

        /**
         * Event - on notice.
         * @param target Target (channel name or user name)
         * @param user User
         * @param msg Message
         */
        public final void onNotice(final String target, final IRCUser user,
            final String msg) {
            Logger.info(
                this,  "%s%s (notice): %s",
                IRCServer.formatChannelPrompt(target), user.getNick(), msg
            );
        }

        /**
         * Event - On part.
         * @param chan Channel
         * @param user User
         * @param msg Message
         */
        public final void onPart(final String chan, final IRCUser user,
            final String msg) {
            Logger.info(
                this, "%s%s parts", IRCServer.formatChannelPrompt(chan),
                user.getNick()
            );
        }

        /**
         * Event - On private message.
         * @param chan Channel
         * @param user User
         * @param msg Message
         */
        public final void onPrivmsg(final String chan, final IRCUser user,
            final String msg) {
            Logger.info(
                this, "%s%s: %s", IRCServer.formatChannelPrompt(chan),
                user.getNick(), msg
            );
        }

        /**
         * Event - on quit.
         * @param user User
         * @param msg Message
         */
        public final void onQuit(final IRCUser user, final String msg) {
            Logger.info(this, "Quit: %s", user.getNick());
        }

        /**
         * Event - on reply.
         * @param num ID
         * @param value Value
         * @param msg Message
         */
        public final void onReply(final int num, final String value,
            final String msg) {
            Logger.info(this, "Reply #%d: %s %s", num, value, msg);
        }

        /**
         * On topic setup.
         * @param chan Channel name
         * @param user User
         * @param topic Topic
         */
        public final void onTopic(final String chan, final IRCUser user,
            final String topic) {
            Logger.info(
                this, "%s%s changes topic into: %s",
                IRCServer.formatChannelPrompt(chan), user.getNick(), topic
            );
        }

        /**
         * On ping.
         * @param ping Ping message
         */
        @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
        public void onPing(final String ping) {
            // not required
        }

        /**
         * On unknown message.
         * @param parta Part a
         * @param partb Part b
         * @param partc Part c
         * @param partd Part D
         * @checkstyle ParameterNumber (3 lines)
         */
        public final void unknown(final String parta, final String partb,
            final String partc, final String partd) {
            Logger.info(
                this, "UNKNOWN: %s %s %s %s", parta, partb, partc, partd
            );
        }
    }

    /**
     * Needed if using SSL.
     */
    @Immutable
    @ToString
    final class TrustManager implements SSLTrustManager {
        /**
         * X509 Certificate chain.
         */
        private transient X509Certificate[] chain;

        /**
         * Get issuers.
         * @return Chain
         */
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] res;
            if (this.chain == null) {
                res = new X509Certificate[0];
            } else {
                res = this.chain.clone();
            }
            return res;
        }

        /**
         * Is trusted.
         * @param chainz Chain
         * @return Is trusted
         */
        public boolean isTrusted(final X509Certificate[] chainz) {
            this.chain = chainz.clone();
            return true;
        }
    }

    /**
     * Helps making the asynchronous IRC API synchronous,
     * which makes it easier to operate.
     */
    @ToString
    @EqualsAndHashCode(of = { "happened", "sleptCounter" })
    final class Waiter {
        /**
         * Flag says if the event has happened.
         */
        private transient boolean happened;

        /**
         * To track the timeouts.
         */
        private transient int sleptCounter;

        /**
         * Sleep time, ms.
         * @checkstyle DeclarationOrder (3 lines)
         * @checkstyle MagicNumber (3 lines)
         */
        private static final int SLEEP_TIME = 100;

        /**
         * Max allowed number of sleeps.
         * @checkstyle DeclarationOrder (3 lines)
         * @checkstyle MagicNumber (3 lines)
         */
        private static final int SLEEP_TIMEOUT = 100;

        /**
         * Event has happened trigger.
         */
        public void triggerHappenned() {
            this.happened = true;
        }

        /**
         * Make thread sleep until event has happened or timeouted.
         */
        public void sleepUntilHappenned() {
            this.sleptCounter = 0;
            while (!this.happened) {
                try {
                    Thread.sleep(this.SLEEP_TIME);
                    if (this.sleptCounter > this.SLEEP_TIMEOUT) {
                        throw new IllegalStateException(
                            String.format("Timeout: %d", this.sleptCounter)
                        );
                    }
                    this.sleptCounter += 1;
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
    }

    /**
     * Format a channel name. Prefix it with "#" sign.
     *
     * @param channelname Channel name
     * @return Formatted channel name
     */
    public static String formatChannelName(final String channelname) {
        return String.format("#%s", channelname);
    }

    /**
     * Formats the receiver of message.
     *
     * @param channelz Receiver
     * @return Formatted
     */
    public static String formatChannelPrompt(final String channelz) {
        return String.format("%s> ", channelz);
    }
}
