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
package com.rultor.shell.ssh;

import com.jcabi.aspects.Loggable;
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
import com.rultor.shell.Shell;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * Single SSH Channel.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@EqualsAndHashCode(of = { "addr", "login", "key" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class SSHChannel implements Shell {

    /**
     * SSH port to use.
     */
    private static final int PORT = 22;

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
                Level jul;
                if (level >= com.jcraft.jsch.Logger.ERROR) {
                    jul = Level.SEVERE;
                } else if (level >= com.jcraft.jsch.Logger.WARN) {
                    jul = Level.WARNING;
                } else {
                    jul = Level.INFO;
                }
                Logger.log(jul, SSHChannel.class, msg);
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
     * User name.
     */
    private final transient String login;

    /**
     * Private SSH key.
     */
    private final transient PrivateKey key;

    /**
     * Public ctor.
     * @param adr IP address
     * @param user Login
     * @param priv Private SSH key
     */
    public SSHChannel(
        @NotNull(message = "IP address can't be NULL") final InetAddress adr,
        @NotNull(message = "user name can't be NULL") final String user,
        @NotNull(message = "private key can't be NULL") final PrivateKey priv) {
        this.addr = adr.getHostAddress();
        Validate.matchesPattern(
            this.addr,
            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
            "Invalid IP address of the server `%s`",
            this.addr
        );
        this.login = user;
        Validate.notEmpty(this.login, "user name can't be empty");
        this.key = priv;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "SSH as `%s` to `%s` with %s",
            this.login, this.addr, this.key
        );
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (10 lines)
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = 1, unit = TimeUnit.HOURS)
    public int exec(@NotNull(message = "command can't be NULL")
        final String command,
        @NotNull(message = "stdin can't be NULL") final InputStream stdin,
        @NotNull(message = "stdout can't be NULL") final OutputStream stdout,
        @NotNull(message = "stderr can't be NULL") final OutputStream stderr)
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
        } catch (JSchException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // nothing to do
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
            } catch (Exception ex) {
                throw new IOException(ex);
            }
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException ex) {
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
        attempts = Tv.TEN,
        delay = 1,
        unit = TimeUnit.MINUTES,
        verbose = false
    )
    private Session session() throws IOException {
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch.setLogger(SSHChannel.LOGGER);
            final JSch jsch = new JSch();
            final File file = this.key.asFile();
            jsch.setHostKeyRepository(SSHChannel.REPO);
            jsch.addIdentity(file.getAbsolutePath());
            Logger.info(
                this,
                "Opening SSH session to %s@%s:%s (%d bytes in RSA key)...",
                this.login, this.addr, SSHChannel.PORT,
                file.length()
            );
            final Session session = jsch.getSession(
                this.login, this.addr, SSHChannel.PORT
            );
            session.setServerAliveInterval((int) TimeUnit.SECONDS.toMillis(1));
            session.setServerAliveCountMax(Tv.MILLION);
            session.connect();
            return session;
        } catch (JSchException ex) {
            throw new IOException(ex);
        }
    }

}
