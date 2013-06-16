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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.rultor.shell.Shell;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Single SSH Channel.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "addr", "login", "key" })
@Loggable(Loggable.DEBUG)
public final class SSHChannel implements Shell {

    /**
     * SSH port to use.
     */
    private static final int PORT = 22;

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
    public SSHChannel(final InetAddress adr, final String user,
        final PrivateKey priv) {
        this.addr = adr.getHostAddress();
        this.login = user;
        this.key = priv;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int exec(@NotNull final String command,
        @NotNull final InputStream stdin,
        @NotNull final OutputStream stdout,
        @NotNull final OutputStream stderr) throws IOException {
        try {
            final Session session = this.session();
            session.connect();
            try {
                final ChannelExec exec = ChannelExec.class.cast(
                    session.openChannel("exec")
                );
                exec.setErrStream(stderr, false);
                exec.setOutputStream(stdout, false);
//                exec.setOutputStream(System.out);
                exec.setInputStream(stdin, false);
                exec.setCommand(command);
                exec.connect();
                try {
                    return this.code(exec);
                } finally {
                    exec.disconnect();
                }
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
     * Wait until it's done and return its code.
     * @param exec The channel
     * @return The exit code
     * @throws IOException If some IO problem inside
     */
    private int code(final ChannelExec exec) throws IOException {
        while (!exec.isClosed()) {
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
     * Create and return a session.
     * @return JSch session
     * @throws IOException If some IO problem inside
     */
    private Session session() throws IOException {
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch.setLogger(
                new com.jcraft.jsch.Logger() {
                    @Override
                    public boolean isEnabled(final int level) {
                        return level == com.jcraft.jsch.Logger.WARN
                            || level == com.jcraft.jsch.Logger.FATAL
                            || level == com.jcraft.jsch.Logger.ERROR;
                    }
                    @Override
                    public void log(final int level, final String msg) {
                        Logger.info(SSHChannel.class, "%s", msg);
                    }
                }
            );
            final JSch jsch = new JSch();
            jsch.addIdentity(this.key.asFile().getAbsolutePath());
            return jsch.getSession(this.login, this.addr, SSHChannel.PORT);
        } catch (com.jcraft.jsch.JSchException ex) {
            throw new IOException(ex);
        }
    }

}
