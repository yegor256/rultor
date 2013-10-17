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

import com.google.common.io.Files;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.PublicKey;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.PublickeyAuthenticator;
import org.apache.sshd.server.UserAuth;
import org.apache.sshd.server.auth.UserAuthPublicKey;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for ${@link SSHChannel}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SSHChannelTest {

    /**
     * SSHChannel can execute command on ssh server.
     * @throws Exception In case of error.
     */
    @Test
    public void executeCommandOnServer() throws Exception {
        final int port = this.port();
        final SshServer sshd = this.sshServer(port);
        sshd.setCommandFactory(new SSHChannelTest.EchoCommandCreator());
        sshd.start();
        final String cmd = "ls";
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final int exit = new SSHChannel(
            InetAddress.getLocalHost(),
            port,
            "test",
            new PrivateKey(
                IOUtils.toString(
                    this.getClass().getResourceAsStream("private.key")
                )
            )
        ).exec(
            cmd,
            Mockito.mock(InputStream.class),
            output,
            Mockito.mock(OutputStream.class)
        );
        sshd.stop();
        MatcherAssert.assertThat(exit, Matchers.equalTo(0));
        MatcherAssert.assertThat(output.toString(), Matchers.equalTo(cmd));
    }

    /**
     * Setup SSH server.
     * @param port Port to listen on.
     * @return SSH server.
     */
    private SshServer sshServer(final int port) {
        final SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        final PublickeyAuthenticator keyAuth =
            Mockito.mock(PublickeyAuthenticator.class);
        Mockito.when(
            keyAuth.authenticate(
                Mockito.anyString(),
                Mockito.any(PublicKey.class),
                Mockito.any(ServerSession.class)
            )
        ).thenReturn(true);
        sshd.setPublickeyAuthenticator(keyAuth);
        sshd.setUserAuthFactories(
            Collections.<NamedFactory<UserAuth>>singletonList(
                new UserAuthPublicKey.Factory()
            )
        );
        sshd.setKeyPairProvider(
            new SimpleGeneratorHostKeyProvider(
                new File(Files.createTempDir(), "hostkey.ser").getAbsolutePath()
            )
        );
        return sshd;
    }

    /**
     * Allocate free port.
     * @return Found port.
     * @throws IOException In case of error.
     */
    private int port() throws IOException {
        final ServerSocket socket = new ServerSocket(0);
        final int port = socket.getLocalPort();
        socket.close();
        return port;
    }

    /**
     * Factory for echo command.
     */
    private static final class EchoCommandCreator implements CommandFactory {
        @Override
        public Command createCommand(final String command) {
            return new SSHChannelTest.EchoCommand(command);
        }
    }

    /**
     * Command that displays its name.
     */
    private static final class EchoCommand implements Command {
        /**
         * Command being executed.
         */
        private final transient String command;

        /**
         * Exit callback.
         */
        private transient ExitCallback callback;

        /**
         * Output stream for use by command.
         */
        private transient OutputStream output;

        /**
         * Constructor.
         * @param cmd Command to echo.
         */
        public EchoCommand(final String cmd) {
            this.command = cmd;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setInputStream(final InputStream input) {
            // do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setOutputStream(final OutputStream out) {
            this.output = out;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setErrorStream(final OutputStream err) {
            // do nothing
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setExitCallback(final ExitCallback cllbck) {
            this.callback = cllbck;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void start(final Environment env) throws IOException {
            IOUtils.write(this.command, this.output);
            this.output.flush();
            this.callback.onExit(0);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void destroy() {
            // do nothing
        }
    }
}
