/**
 * Copyright (c) 2009-2017, rultor.com
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
 * Remove old images from Docker.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.57
 */
public final class DockerExec implements SuperAgent {

    /**
     * Shell to use.
     */
    private final transient Shell shell;

    /**
     * Script to exec.
     */
    private final transient String script;

    /**
     * Ctor.
     * @param ssh Shell
     * @param scrpt Script
     */
    public DockerExec(final Shell ssh, final String scrpt) {
        this.shell = ssh;
        this.script = scrpt;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        new Shell.Safe(this.shell).exec(
            IOUtils.toString(
                this.getClass().getResourceAsStream(this.script)
            ),
            new NullInputStream(0L),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
    }

}
