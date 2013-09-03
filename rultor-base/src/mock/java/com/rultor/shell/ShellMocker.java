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
package com.rultor.shell;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assume;

/**
 * Mocker of {@link Shell}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class ShellMocker {

    /**
     * Shell to local bash.
     */
    @Immutable
    public static final class Bash implements Shell {
        /**
         * Directory to work in.
         */
        private final transient String dir;
        /**
         * Public ctor.
         * @param folder Where to work
         */
        public Bash(final File folder) {
            Assume.assumeFalse(SystemUtils.IS_OS_WINDOWS);
            this.dir = folder.getAbsolutePath();
        }
        // @checkstyle ParameterNumber (5 lines)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream stdout, final OutputStream stderr)
            throws IOException {
            Logger.info(this, "$ %s", command);
            final Process process = new ProcessBuilder()
                .command("bash", "-c", command)
                .directory(new File(this.dir))
                .start();
            final OutputStream out = process.getOutputStream();
            IOUtils.copy(stdin, out);
            out.close();
            stdin.close();
            IOUtils.copy(process.getInputStream(), stdout);
            IOUtils.copy(process.getErrorStream(), stderr);
            try {
                return process.waitFor();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IOException(ex);
            }
        }
        @Override
        public void close() throws IOException {
            assert this.dir != null;
        }
    }

    /**
     * Mock it.
     * @return Shell
     */
    public Shell mock() {
        throw new UnsupportedOperationException();
    }

}
