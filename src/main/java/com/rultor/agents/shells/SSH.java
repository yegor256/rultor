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

import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.Validate;

/**
 * Single SSH Channel.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode(of = { "addr", "port", "login", "key" })
@SuppressWarnings("PMD.TooManyMethods")
public final class SSH implements Shell {

    /**
     * Logger to use for all channels.
     */
    private static final com.jcraft.jsch.Logger LOGGER =
        new com.jcraft.jsch.Logger() {
            @Override
            public boolean isEnabled(final int level) {
                return level >= com.jcraft.jsch.Logger.WARN;
            }
            @Override
            public void log(final int level, final String msg) {
                final Level jul;
                if (level >= com.jcraft.jsch.Logger.ERROR) {
                    jul = Level.SEVERE;
                } else if (level >= com.jcraft.jsch.Logger.WARN) {
                    jul = Level.WARNING;
                } else {
                    jul = Level.INFO;
                }
                Logger.log(jul, SSH.class, msg);
            }
        };

    /**
     * Host key repository that accepts all hosts.
     * @checkstyle AnonInnerLengthCheck (40 lines)
     */
    private static final HostKeyRepository REPO = new HostKeyRepository() {
        @Override
        public int check(final String host, final byte[] bkey) {
            return HostKeyRepository.OK;
        }
        @Override
        public void add(final HostKey hostkey, final UserInfo info) {
            // do nothing
        }
        @Override
        public void remove(final String host, final String type) {
            // do nothing
        }
        @Override
        public void remove(final String host, final String type,
            final byte[] bkey) {
            // do nothing
        }
        @Override
        public String getKnownHostsRepositoryID() {
            return "";
        }
        @Override
        public HostKey[] getHostKey() {
            return new HostKey[0];
        }
        @Override
        public HostKey[] getHostKey(final String host, final String type) {
            return new HostKey[0];
        }
    };

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
     * @throws UnknownHostException If fails
     * @checkstyle ParameterNumberCheck (6 lines)
     */
    public SSH(final String adr, final int prt,
        final String user, final String priv) throws UnknownHostException {
        this.addr = InetAddress.getByName(adr).getHostAddress();
        Validate.matchesPattern(
            this.addr,
            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            "Invalid IP address of the server `%s`",
            this.addr
        );
        this.login = user;
        Validate.notEmpty(this.login, "user name can't be empty");
        this.key = priv;
        this.port = prt;
    }

    // @checkstyle ParameterNumberCheck (5 lines)
    @Override
    public int exec(final String command, final InputStream stdin,
        final OutputStream stdout, final OutputStream stderr)
        throws IOException {
        try {
            final Session session = this.session();
            try {
                final ChannelExec channel = ChannelExec.class.cast(
                    session.openChannel("exec")
                );
                channel.setErrStream(stderr, false);
                channel.setOutputStream(stdout, false);
                channel.setInputStream(stdin, false);
                channel.setCommand(command);
                channel.connect();
                Logger.info(this, "$ %s", command);
                return this.exec(channel, session);
            } finally {
                session.disconnect();
            }
        } catch (final JSchException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Exec this channel and return its exit code.
     * @param channel The channel to exec
     * @param session The session
     * @return Exit code (zero in case of success)
     * @throws IOException If fails
     */
    private int exec(final ChannelExec channel, final Session session)
        throws IOException {
        try {
            return this.code(channel, session);
        } finally {
            channel.disconnect();
        }
    }

    /**
     * Wait until it's done and return its code.
     * @param exec The channel
     * @param session The session
     * @return The exit code
     * @throws IOException If some IO problem inside
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private int code(final ChannelExec exec, final Session session)
        throws IOException {
        while (!exec.isClosed()) {
            try {
                session.sendKeepAliveMsg();
                // @checkstyle IllegalCatch (1 line)
            } catch (final Exception ex) {
                throw new IOException(ex);
            }
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
        }
        return exec.getExitStatus();
    }

    /**
     * Create and return a session, connected.
     * @return JSch session
     * @throws IOException If some IO problem inside
     */
    @RetryOnFailure(
        attempts = Tv.SEVEN,
        delay = 1,
        unit = TimeUnit.MINUTES,
        verbose = false,
        randomize = true,
        types = IOException.class
    )
    private Session session() throws IOException {
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch.setLogger(SSH.LOGGER);
            final JSch jsch = new JSch();
            final File file = File.createTempFile("rultor", ".key");
            FileUtils.forceDeleteOnExit(file);
            FileUtils.write(
                file,
                this.key.replaceAll("\r", "")
                    .replaceAll("\n\\s+|\n{2,}", "\n")
                    .trim(),
                CharEncoding.UTF_8
            );
            jsch.setHostKeyRepository(SSH.REPO);
            jsch.addIdentity(file.getAbsolutePath());
            Logger.debug(
                this,
                "Opening SSH session to %s@%s:%s (%d bytes in RSA key)...",
                this.login, this.addr, this.port,
                file.length()
            );
            final Session session = jsch.getSession(
                this.login, this.addr, this.port
            );
            session.setServerAliveInterval(
                (int) TimeUnit.SECONDS.toMillis((long) Tv.TEN)
            );
            session.setServerAliveCountMax(Tv.MILLION);
            session.connect();
            FileUtils.deleteQuietly(file);
            return session;
        } catch (final JSchException ex) {
            throw new IOException(ex);
        }
    }

}
