/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.docker;

import com.jcabi.ssh.Shell;
import com.rultor.spi.Talks;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for ${@link DockerHealthCheck}.
 *
 * @since 1.63
 */
final class DockerHealthCheckTest {

    /**
     * DockerHealthCheckTest can execute checkhost.sh.
     * @throws Exception In case of error
     */
    @Test
    void runsCheckHostScript() throws Exception {
        final Shell shell = Mockito.mock(Shell.class);
        new DockerHealthCheck(shell).execute(Mockito.mock(Talks.class));
        Mockito.verify(shell).exec(
            Mockito.eq(
                IOUtils.toString(
                    DockerHealthCheck.class.getResource("checkhost.sh"),
                    StandardCharsets.UTF_8
                )
            ),
            Mockito.any(InputStream.class),
            Mockito.any(OutputStream.class),
            Mockito.any(OutputStream.class)
        );
    }

}
