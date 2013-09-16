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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.security.cert.X509Certificate;
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLIRCConnection;
import org.schwering.irc.lib.ssl.SSLTrustManager;

/**
 * Actually a factory of a connection object.
 *
 * @author Konstantin Voytenko (cppruler@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public class IRCServer {
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
     * Creates a connection.
     *
     * @param channel Channel
     * @param pass Password
     * @param nick Nickname
     * @param user User
     * @param name Real name
     * @param ssl Is SSL used
     * @return Connection
     * @checkstyle DesignForExtension (4 lines) This method be overridden
     *  by the Mock implementation. So cannot make it final
     * @checkstyle ParameterNumber (4 lines)
     */
    public IRCConnection connect(final String channel,
        final String pass, final String nick,
        final String user, final String name, final boolean ssl) {
        final IRCConnection conn = this.connectionInstantiate(
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
     */
    private void connectToServerAndJoinChannel(final IRCConnection conn,
        final String channel) {
        final Waiter waiterConnected = new Waiter();
        final Waiter waiterJoined = new Waiter();
        this.connSetOptions(conn, waiterConnected, waiterJoined);
        this.connConnect(conn);
        waiterConnected.sleepUntilHappenned();
        this.joinChannel(conn, channel);
        waiterJoined.sleepUntilHappenned();
    }

    /**
     * Instantiates a connection object to talk to IRC server.
     *
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
    private IRCConnection connectionInstantiate(final String hostz,
        final int portz, final String pass, final String nick,
        final String user, final String name, final boolean ssl) {
        IRCConnection connz;
        if (ssl) {
            connz = new SSLIRCConnection(hostz, new int[]{portz}, pass,
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
                    UtilLogger.print(
                        String.format(
                            "%s%s joins",
                            UtilFormatter.formatChannelPrompt(chan),
                            user.getNick()
                        )
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
     * Start the connection to the server.
     * @param conn Connection
     */
    private void connConnect(final IRCConnection conn) {
        try {
            conn.connect();
        } catch (IOException ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    /**
     * Join the channel.
     * @param conn Connection
     * @param channelz Channel name
     */
    private void joinChannel(final IRCConnection conn,
        final String channelz) {
        final String command = String.format(
            "join %s", UtilFormatter.formatChannelName(channelz)
        );
        UtilLogger.print(command);
        conn.send(command);
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
            UtilLogger.print("Disconnected");
        }

        /**
         * Event - On error.
         * @param msg Error message
         */
        public final void onError(final String msg) {
            UtilLogger.print(String.format("Error: %s", msg));
        }

        /**
         * Event - On error.
         * @param num Error num
         * @param msg Error message
         */
        public final void onError(final int num, final String msg) {
            UtilLogger.print(
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
            UtilLogger.print(
                String.format(
                    "%s%s invites %s",
                    UtilFormatter.formatChannelPrompt(chan),
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
            UtilLogger.print(
                String.format(
                    "%s%s kicks %s",
                    UtilFormatter.formatChannelPrompt(chan),
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
            UtilLogger.print(
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
            UtilLogger.print(
                String.format(
                    "%s%s sets mode: %s",
                    UtilFormatter.formatChannelPrompt(chan),
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
            UtilLogger.print(
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
            UtilLogger.print(
                String.format(
                    "%s%s (notice): %s",
                    UtilFormatter.formatChannelPrompt(target),
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
            UtilLogger.print(
                String.format(
                    "%s%s parts",
                    UtilFormatter.formatChannelPrompt(chan), user.getNick()
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
            UtilLogger.print(
                String.format(
                    "%s%s: %s",
                    UtilFormatter.formatChannelPrompt(chan),
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
            UtilLogger.print(String.format("Quit: %s", user.getNick()));
        }

        /**
         * Event - on reply.
         * @param num ID
         * @param value Value
         * @param msg Message
         */
        public final void onReply(final int num, final String value,
            final String msg) {
            UtilLogger.print(
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
            UtilLogger.print(
                String.format(
                    "%s%s changes topic into: %s",
                    UtilFormatter.formatChannelPrompt(chan),
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
            UtilLogger.print(
                String.format(
                    "UNKNOWN: %s %s %s %s",
                    parta, partb, partc, partd
                )
            );
        }
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
     * String formatter utility.
     */
    static final class UtilFormatter {
        /**
         * Private ctor.
         * Class is supposed to only be used statically
         */
        private UtilFormatter() {
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

    /**
     * Logger utility.
     */
    static final class UtilLogger {
        /**
         * Private ctor.
         * Class is supposed to only be used statically
         */
        private UtilLogger() {
        }

        /**
         * A shorthand for the Logger.info method.
         *
         * @param obj Object to be printed
         */
        public static void print(final Object obj) {
            Logger.info(Object.class, (String) obj);
        }

        /**
         * A shorthand for the Logger.info method.
         *
         * @param obj Object to be printed
         */
        public static void printFromProgram(final Object obj) {
            Logger.info(Object.class, String.format("[i am]  %s", obj));
        }
    }
}
