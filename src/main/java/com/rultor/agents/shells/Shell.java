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

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.CharEncoding;

/**
 * Shell.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Shell {

    /**
     * Execute and return exit code.
     * @param command Command
     * @param stdin Stdin (will be closed)
     * @param stdout Stdout (will be closed)
     * @param stderr Stderr (will be closed)
     * @return Exit code
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (5 line)
     */
    int exec(String command, InputStream stdin,
        OutputStream stdout, OutputStream stderr) throws IOException;

    /**
     * Safe run (throws if exit code is not zero).
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Safe implements Shell {
        /**
         * Original.
         */
        private final transient Shell origin;
        /**
         * Ctor.
         * @param shell Original shell
         */
        public Safe(final Shell shell) {
            this.origin = shell;
        }
        // @checkstyle ParameterNumberCheck (5 line)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream stdout, final OutputStream stderr)
            throws IOException {
            final int exit = this.origin.exec(command, stdin, stdout, stderr);
            if (exit != 0) {
                throw new IllegalArgumentException(
                    String.format("exit code #%d: %s", exit, command)
                );
            }
            return exit;
        }
    }

    /**
     * Without input and output.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Empty {
        /**
         * Original.
         */
        private final transient Shell origin;
        /**
         * Ctor.
         * @param shell Original shell
         */
        public Empty(final Shell shell) {
            this.origin = shell;
        }
        /**
         * Just exec.
         * @param cmd Command
         * @return Exit code
         * @throws IOException If fails
         */
        public int exec(final String cmd) throws IOException {
            return this.origin.exec(
                cmd, new NullInputStream(0L),
                Logger.stream(Level.INFO, this),
                Logger.stream(Level.WARNING, this)
            );
        }
    }

    /**
     * With output only.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "origin")
    final class Plain {
        /**
         * Original.
         */
        private final transient Shell origin;
        /**
         * Ctor.
         * @param shell Original shell
         */
        public Plain(final Shell shell) {
            this.origin = shell;
        }
        /**
         * Just exec.
         * @param cmd Command
         * @return Stdout
         * @throws IOException If fails
         */
        public String exec(final String cmd) throws IOException {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            this.origin.exec(
                cmd, new NullInputStream(0L),
                baos, baos
            );
            return baos.toString(CharEncoding.UTF_8);
        }
    }

    /**
     * Verbose run.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "orgn")
    final class Verbose implements Shell {
        /**
         * Original.
         */
        private final transient Shell orgn;
        /**
         * Ctor.
         * @param shell Original shell
         */
        public Verbose(final Shell shell) {
            this.orgn = shell;
        }
        // @checkstyle ParameterNumberCheck (5 line)
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream stdout, final OutputStream stderr)
            throws IOException {
            return this.orgn.exec(
                command, stdin,
                new TeeOutputStream(stdout, Logger.stream(Level.INFO, this)),
                new TeeOutputStream(stderr, Logger.stream(Level.WARNING, this))
            );
        }
    }
}
