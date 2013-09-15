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
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLIRCConnection;
import org.schwering.irc.lib.ssl.SSLTrustManager;

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
@SuppressWarnings("PMD.TooManyMethods")
public final class IRC implements Billboard {

    /**
     * Host name.
     */
    private final transient String host;

    /**
     * Port number.
     */
    private final transient int port;

    /**
     * Channel name.
     * Or generally speaking:
     * target of PRIVMSGs (a channel or nickname)
     */
    private final transient String channel;

    /**
     * The IRC connection.
     */
    private final transient IRCConnection conn;

    /**
     * Public ctor.
     *
     * @param hst Host
     * @param prt Port
     * @param chnl Channel
     */
    public IRC(final String hst, final int prt, final String chnl) {
        this.host = hst;
        this.port = prt;
        this.channel = chnl;
        this.conn = this.connInstantiate(
            this.host, this.port, "", "nickTest", "userTest", "nameTest",
            false
        );
    }

    /**
     * Public ctor.
     *
     * @param hst Host
     * @param prt Port
     * @param chnl Channel
     * @param connz Connection with predefined host and port.
     *  Supposed to be already connected to the server.
     * @checkstyle ParameterNumber (3 lines)
     */
    public IRC(final String hst, final int prt, final String chnl,
        final IRCConnection connz) {
        this.host = hst;
        this.port = prt;
        this.channel = chnl;
        this.conn = connz;
    }

    /**
     * Connect the server and join the channel.
     */
    public void connectAndJoinChannel() {
        final Waiter waiterConnected = new Waiter();
        final Waiter waiterJoined = new Waiter();
        this.connSetOptions(waiterConnected, waiterJoined);
        this.connConnect();
        waiterConnected.sleepUntilHappenned();
        this.joinChannel(this.channel);
        waiterJoined.sleepUntilHappenned();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void announce(
        @NotNull(message = "body can't be NULL") final String body) {
        if (!this.conn.isConnected()) {
            this.connectAndJoinChannel();
        }
        final String channelFormatted =
            this.formatChannelName(this.channel);
        this.conn.doPrivmsg(channelFormatted, body);
        this.printFromProgram(
            String.format(
                "%s%s",
                this.formatToChannel(channelFormatted), body
            )
        );
    }

    /**
     * Instantiates a connection object to talk to IRC server.
     * @param hostz Host
     * @param portz Port
     * @param pass Password
     * @param nick Nickname
     * @param user Username
     * @param name User real name
     * @param ssl Is SSL
     * @return IRCConnection
     * @checkstyle ParameterNumber (4 lines)
     */
    private IRCConnection connInstantiate(final String hostz, final int portz,
        final String pass, final String nick,
        final String user, final String name, final boolean ssl) {
        IRCConnection connz = null;
        if (ssl) {
            connz =
                new SSLIRCConnection(hostz, new int[]{portz}, pass,
                    nick, user, name);
            ((SSLIRCConnection) connz).addTrustManager(new TrustManager());
        } else {
            connz = new IRCConnection(hostz, new int[]{portz}, pass,
                nick, user, name);
        }
        return connz;
    }

    /**
     * Setups common IRC protocol options.
     *
     * @param connected Waiter until connected to server
     * @param joined Waiter until joined the server
     */
    private void connSetOptions(final Waiter connected,
        final Waiter joined) {
        this.conn.addIRCEventListener(
            new AbstractListener() {
                @Override
                public void onRegistered() {
                    connected.triggerHappenned();
                }
                @Override
                public void onJoin(final String chan, final IRCUser user) {
                    IRC.this.print(
                        String.format(
                            "%s%s joins",
                            IRC.this.formatToChannel(chan), user.getNick()
                        )
                    );
                    joined.triggerHappenned();
                }
            }
        );
        this.conn.setEncoding("UTF-8");
        this.conn.setPong(true);
        this.conn.setDaemon(false);
        this.conn.setColors(false);
    }

    /**
     * Start the connection to the server.
     */
    private void connConnect() {
        try {
            this.conn.connect();
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * Do join the channel.
     *
     * @param channelz Channel name
     */
    private void joinChannel(final String channelz) {
        final String command = String.format(
            "join %s", this.formatChannelName(channelz)
        );
        this.print(command);
        this.conn.send(command);
    }

    /**
     * Format a channel name. Prefix it with "#" sign.
     *
     * @param channelname Channel name
     * @return Formatted channel name
     */
    private String formatChannelName(final String channelname) {
        return String.format("#%s", channelname);
    }

    /**
     * Needed if using SSL.
     */
    public class TrustManager implements SSLTrustManager {
        /**
         * X509 Certificate chain.
         */
        private transient X509Certificate[] chain;

        /**
         * Get issuers.
         * @return Chain
         */
        public final X509Certificate[] getAcceptedIssuers() {
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
        public final boolean isTrusted(final X509Certificate[] chainz) {
            this.chain = chainz.clone();
            return true;
        }
    }

    /**
     * Treats IRC events. The most of them are just printed.
     */
    abstract class AbstractListener implements IRCEventListener {

        /**
         * Event - On Registered.
         */
        public abstract void onRegistered();

        /**
         * Event - On disconnected.
         */
        public final void onDisconnected() {
            IRC.this.print("Disconnected");
        }

        /**
         * Event - On error.
         * @param msg Error message
         */
        public final void onError(final String msg) {
            IRC.this.print(String.format("Error: %s", msg));
        }

        /**
         * Event - On error.
         * @param num Error num
         * @param msg Error message
         */
        public final void onError(final int num, final String msg) {
            IRC.this.print(
                String.format("Error #%d: %s", num, msg)
            );
        }

        /**
         * Event - On invitation.
         * @param chan Channel name
         * @param user User
         * @param nickpass Password
         */
        public final void onInvite(final String chan, final IRCUser user,
            final String nickpass) {
            IRC.this.print(
                String.format(
                    "%s%s invites %s",
                    IRC.this.formatToChannel(chan),
                    user.getNick(), nickpass
                )
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
            IRC.this.print(
                String.format(
                    "%s%s kicks %s",
                    IRC.this.formatToChannel(chan),
                    user.getNick(), nickpass
                )
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
            IRC.this.print(
                String.format(
                    "Mode: %s sets modes %s %s",
                    user.getNick(), mode, nickpass
                )
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
            IRC.this.print(
                String.format(
                    "%s%s sets mode: %s",
                    IRC.this.formatToChannel(chan),
                    user.getNick(), modeparser.getLine()
                )
            );
        }

        /**
         * Event - On nickname change.
         * @param user User
         * @param nicknew New nickname
         */
        public final void onNick(final IRCUser user, final String nicknew) {
            IRC.this.print(
                String.format(
                    "Nick: %s is now known as %s",
                    user.getNick(), nicknew
                )
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
            IRC.this.print(
                String.format(
                    "%s%s (notice): %s",
                    IRC.this.formatToChannel(target),
                    user.getNick(), msg
                )
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
            IRC.this.print(
                String.format(
                    "%s%s parts",
                    IRC.this.formatToChannel(chan), user.getNick()
                )
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
            IRC.this.print(
                String.format(
                    "%s%s: %s",
                    IRC.this.formatToChannel(chan),
                    user.getNick(), msg
                )
            );
        }

        /**
         * Event - on quit.
         * @param user User
         * @param msg Message
         */
        public final void onQuit(final IRCUser user, final String msg) {
            IRC.this.print(String.format("Quit: %s", user.getNick()));
        }

        /**
         * Event - on reply.
         * @param num ID
         * @param value Value
         * @param msg Message
         */
        public final void onReply(final int num, final String value,
            final String msg) {
            IRC.this.print(
                String.format("Reply #%d: %s %s", num, value, msg)
            );
        }

        /**
         * On topic setup.
         * @param chan Channel name
         * @param user User
         * @param topic Topic
         */
        public final void onTopic(final String chan, final IRCUser user,
            final String topic) {
            IRC.this.print(
                String.format(
                    "%s%s changes topic into: %s",
                    IRC.this.formatToChannel(chan),
                    user.getNick(), topic
                )
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
            IRC.this.print(
                String.format(
                    "UNKNOWN: %s %s %s %s",
                    parta, partb, partc, partd
                )
            );
        }
    }

    /**
     * Helps making the asynchronous IRC API synchronous,
     * which makes it easier to operate.
     */
    class Waiter {
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
        public final void triggerHappenned() {
            this.happened = true;
        }

        /**
         * Make thread sleep until event has happened or timeouted.
         */
        public final void sleepUntilHappenned() {
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
     * A shorthand for the Logger.info method.
     *
     * @param obj Object to be printed
     */
    private void print(final Object obj) {
        Logger.info(this, (String) obj);
    }

    /**
     * A shorthand for the Logger.info method.
     *
     * @param obj Object to be printed
     */
    private void printFromProgram(final Object obj) {
        Logger.info(this, String.format("[i am]  %s", obj));
    }

    /**
     * Formats the receiver of message.
     *
     * @param channelz Receiver
     * @return Formatted
     */
    private String formatToChannel(final String channelz) {
        return String.format("%s> ", channelz);
    }
}
