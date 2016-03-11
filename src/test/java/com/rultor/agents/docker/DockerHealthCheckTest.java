/**
 * Copyright (c) 2009-2016, rultor.com
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
package com.rultor.agents.docker;

import com.jcabi.ssh.Shell;
import com.rultor.spi.Talks;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for ${@link DockerHealthCheck}.
 *
 * @author Armin Braun (me@obrown.io)
 * @version $Id$
 * @since 1.63
 */
public final class DockerHealthCheckTest {

    /**
     * DockerHealthCheckTest can execute checkhost.sh.
     * @throws Exception In case of error
     */
    @Test
    public void runsCheckHostScript() throws Exception {
        final Shell shell = Mockito.mock(Shell.class);
        new DockerHealthCheck(shell).execute(Mockito.mock(Talks.class));
        Mockito.verify(shell).exec(
            Mockito.eq(
                IOUtils.toString(
                    DockerHealthCheck.class.getResourceAsStream("checkhost.sh")
                )
            ),
            Mockito.any(InputStream.class),
            Mockito.any(OutputStream.class),
            Mockito.any(OutputStream.class)
        );
    }

}
