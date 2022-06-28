/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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

import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;

/**
 * Checks the Health of a Docker host and tries to recover Docker daemon
 * crashes.
 * @author Armin Braun (me@obrown.io)
 * @version $Id$
 * @since 1.63
 * @todo #1041:30min Add DockerHealthCheck to the running SuperAgents.
 *  In doing so make sure that Rultor crashes throwing a meaningful exception
 *  as soon as DockerHealthCheck#execute throws and exception.
 *  #1041 has the details on the motivation behind this agent.
 */
public final class DockerHealthCheck implements SuperAgent {

    /**
     * Shell to use.
     */
    private final transient Shell shell;

    /**
     * Ctor.
     * @param ssh Shell
     */
    public DockerHealthCheck(final Shell ssh) {
        this.shell = ssh;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        new Shell.Safe(this.shell).exec(
            IOUtils.toString(
                this.getClass().getResourceAsStream("checkhost.sh")
            ),
            new NullInputStream(0L),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
    }

}
