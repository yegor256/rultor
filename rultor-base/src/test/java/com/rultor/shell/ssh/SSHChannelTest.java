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
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for ${@link SSHChannel}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
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
        final CommandFactory cmdFactory = this.cmdFactory();
        sshd.setCommandFactory(cmdFactory);
        sshd.start();
        final String cmd = "ls";
        new SSHChannel(
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
            Mockito.mock(OutputStream.class),
            Mockito.mock(OutputStream.class)
        );
        sshd.stop();
        Mockito.verify(cmdFactory).createCommand(Mockito.eq(cmd));
    }

    /**
     * Create command factory.
     * @return Command factory.
     * @throws IOException In case of error.
     */
    private CommandFactory cmdFactory() throws IOException {
        final CommandFactory commands = Mockito.mock(CommandFactory.class);
        final Command cmd = Mockito.mock(Command.class);
        final ExitCallback[] callback = new ExitCallback[1];
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock invocation) {
                    callback[0] = (ExitCallback) invocation.getArguments()[0];
                    return null;
                }
            }
        ).when(cmd).setExitCallback(Mockito.any(ExitCallback.class));
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock invocation) {
                    callback[0].onExit(1);
                    return null;
                }
            }
        ).when(cmd).start(Mockito.any(Environment.class));
        Mockito.when(commands.createCommand(Mockito.anyString()))
            .thenReturn(cmd);
        return commands;
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
            new SimpleGeneratorHostKeyProvider("hostkey.ser")
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
}
