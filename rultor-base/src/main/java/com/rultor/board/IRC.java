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
import org.schwering.irc.lib.IRCConnection;
import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;
import org.schwering.irc.lib.ssl.SSLIRCConnection;
import org.schwering.irc.lib.ssl.SSLTrustManager;

/**
 * IRC.
 * The IRC client API implementation is from here:
 * http://moepii.sourceforge.net/
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "host", "port", "channel" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class IRC implements Billboard {
    /**
     * Host name.
     */
    private transient String host;

    /**
     * Port number.
     */
    private transient int port;

    /**
     * Channel name.
     * Or generally speaking:
     * target of PRIVMSGs (a channel or nickname)
     */
    private final transient String channel;

    /**
     * The IRC connection.
     */
    private transient IRCConnection conn;

    /**
     * Waiter for event: Connected to IRC server.
     */
    private final transient Waiter waiterConnected = new Waiter();

    /**
     * Waiter for event: Joined IRC channel.
     */
    private final transient Waiter waiterJoined = new Waiter();

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
        this.connInstantiate(
            this.host, this.port, "", "nickTest", "userTest", "nameTest",
            false
        );
        this.connSetOptions();
        this.connectAndJoin();
    }

    /**
     * Public ctor.
     *
     * @param chnl Channel
     * @param connz Connection with predefined host and port. Supposed to be already connected to the server.
     */
    public IRC(final String chnl, final IRCConnection connz) {
        this.channel = chnl;
        this.conn = connz;
        this.connSetOptions();
    }

    /**
     * Connect the server and join the channel.
     */
    public void connectAndJoin() {
        this.connConnect();
        this.waiterConnected.sleepUntilHappenned();
        this.joinChannel(this.channel);
        this.waiterJoined.sleepUntilHappenned();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "IRC channel `%s` at `%s:%d`",
            this.channel, this.host, this.port
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void announce(
        @NotNull(message = "body can't be NULL") final String body) {
        assert this.channel != null;
        final String channelFormatted =
            this.formatChannelName(this.channel);
        this.conn.doPrivmsg(channelFormatted, body);
        this.printFromProgram(this.formatToChannel(channelFormatted)
            + body
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
     * @checkstyle ParameterNumber (4 lines)
     */
    @SuppressWarnings("PMD.ConfusingTernary")
    private void connInstantiate(final String hostz, final int portz,
        final String pass, final String nick,
        final String user, final String name, final boolean ssl) {
        if (!ssl) {
            this.conn = new IRCConnection(hostz, new int[]{portz}, pass,
                nick, user, name);
        } else {
            this.conn = new SSLIRCConnection(hostz, new int[]{portz}, pass,
                nick, user, name);
            ((SSLIRCConnection) this.conn).addTrustManager(new TrustManager());
        }
    }

    /**
     * Setups common IRC protocol options.
     */
    private void connSetOptions() {
        this.conn.addIRCEventListener(
            new Listener() {
                @Override
                public void onRegistered() {
                    super.onRegistered();
                    IRC.this.waiterConnected.triggerHappenned();
                }
                @Override
                public void onJoin(final String chan, final IRCUser user) {
                    super.onJoin(chan, user);
                    IRC.this.waiterJoined.triggerHappenned();
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
        // @checkstyle StringLiteralsConcatenation (1 line)
        final String command = "join " + this.formatChannelName(channelz);
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
        // @checkstyle StringLiteralsConcatenation (1 line)
        return "#" + channelname;
    }

    /**
     * Needed if use SSL.
     * @checkstyle JavadocMethod (20 lines)
     */
    @SuppressWarnings({ "PMD.ConfusingTernary", "PMD.ArrayIsStoredDirectly" })
    public class TrustManager implements SSLTrustManager {
        // @checkstyle JavadocVariable (1 line)
        private transient X509Certificate[] chain;

        public final X509Certificate[] getAcceptedIssuers() {
            // @checkstyle AvoidInlineConditionals (1 line)
            return this.chain != null ? this.chain : new X509Certificate[0];
        }

        public final boolean isTrusted(final X509Certificate[] chainz) {
            this.chain = chainz;
            return true;
        }
    }

    /**
     * Treats IRC events. The most of them are just printed.
     * @checkstyle JavadocMethod (110 lines)
     * @checkstyle DesignForExtension (110 lines)
     * @checkstyle FinalParameters (110 lines)
     * @checkstyle ParameterName (110 lines)
     * @checkstyle StringLiteralsConcatenation (110 lines)
     * @checkstyle MultipleStringLiterals (110 lines)
     */
    @SuppressWarnings("PMD.MethodArgumentCouldBeFinal")
    public class Listener implements IRCEventListener {
        public void onRegistered() {
            IRC.this.print("Connected");
            IRC.this.joinChannel(IRC.this.channel);
        }

        public void onDisconnected() {
            IRC.this.print("Disconnected");
        }

        public void onError(String msg) {
            // @checkstyle StringLiteralsConcatenation (1 line)
            IRC.this.print("Error: " + msg);
        }

        public void onError(int num, String msg) {
            IRC.this.print("Error #" + num + ": " + msg);
        }

        public void onInvite(String chan, IRCUser u, String nickPass) {
            IRC.this.print(IRC.this.formatToChannel(chan) + u.getNick()
                + " invites " + nickPass
            );
        }

        public void onJoin(String chan, IRCUser u) {
            IRC.this.print(IRC.this.formatToChannel(chan) + u.getNick()
                + " joins"
            );
        }

        // @checkstyle ParameterNumber (1 line)
        public void onKick(String chan, IRCUser u, String nickPass,
            String msg) {
            IRC.this.print(IRC.this.formatToChannel(chan) + u.getNick()
                + " kicks " + nickPass
            );
        }

        public void onMode(IRCUser u, String nickPass, String mode) {
            IRC.this.print(
                "Mode: " + u.getNick() + " sets modes " + mode + " "
                    + nickPass
            );
        }

        public void onMode(String chan, IRCUser u, IRCModeParser mp) {
            IRC.this.print(IRC.this.formatToChannel(chan) + u.getNick()
                + " sets mode: " + mp.getLine()
            );
        }

        public void onNick(IRCUser u, String nickNew) {
            IRC.this.print("Nick: " + u.getNick() + " is now known as "
                + nickNew
            );
        }

        public void onNotice(String target, IRCUser u, String msg) {
            IRC.this.print(IRC.this.formatToChannel(target) + u.getNick()
                + " (notice): " + msg
            );
        }

        public void onPart(String chan, IRCUser u, String msg) {
            IRC.this.print(IRC.this.formatToChannel(chan) + u.getNick()
                + " parts"
            );
        }

        public void onPrivmsg(String chan, IRCUser u, String msg) {
            IRC.this.print(IRC.this.formatToChannel(chan) + u.getNick()
                + ": " + msg
            );
        }

        public void onQuit(IRCUser u, String msg) {
            IRC.this.print("Quit: " + u.getNick());
        }

        public void onReply(int num, String value, String msg) {
            IRC.this.print("Reply #" + num + ": " + value + " " + msg);
        }

        public void onTopic(String chan, IRCUser u, String topic) {
            IRC.this.print(IRC.this.formatToChannel(chan) + u.getNick()
                + " changes topic into: " + topic
            );
        }

        @SuppressWarnings("PMD.UncommentedEmptyMethod")
        public void onPing(String p) {
        }

        // @checkstyle ParameterNumber (1 line)
        public void unknown(String a, String b, String c, String d) {
            IRC.this.print("UNKNOWN: " + a + " b " + c + " " + d);
        }
    }

    /**
     * Helps making the asynchronous IRC API synchronous,
     * which makes it easier to operate.
     */
    public class Waiter {
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
                    // @checkstyle StringLiteralsConcatenation (3 lines)
                    if (this.sleptCounter > this.SLEEP_TIMEOUT) {
                        throw new IllegalStateException("Timeout: "
                            + this.sleptCounter);
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
        // @checkstyle StringLiteralsConcatenation (1 line)
        Logger.info(this, "[i am] " + obj);
    }

    /**
     * Formats the receiver of message.
     *
     * @param channelz Receiver
     * @return Formatted
     */
    private String formatToChannel(final String channelz) {
        // @checkstyle StringLiteralsConcatenation (1 line)
        return channelz + "> ";
    }
}
