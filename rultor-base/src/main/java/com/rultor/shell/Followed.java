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
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Shells followed on closing.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class Followed implements Shells {

    /**
     * Original.
     */
    private final transient Shells origin;

    /**
     * Sequel.
     */
    private final transient Sequel sequel;

    /**
     * Public ctor.
     * @param seql Sequel
     * @param shells Shells
     */
    public Followed(
        @NotNull(message = "sequael can't be NULL") final Sequel seql,
        @NotNull(message = "shells can't be NULL") final Shells shells) {
        this.sequel = seql;
        this.origin = shells;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s followed by %s", this.origin, this.sequel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shell acquire() throws IOException {
        return new Followed.Sequeled(this.origin.acquire(), this.sequel);
    }

    /**
     * Followed with sequel.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "origin", "sequel" })
    @Loggable(Loggable.DEBUG)
    private static final class Sequeled implements Shell {
        /**
         * Original.
         */
        private final transient Shell origin;
        /**
         * Sequel.
         */
        private final transient Sequel sequel;
        /**
         * Public ctor.
         * @param shell Original shell
         * @param seql Sequel
         */
        protected Sequeled(final Shell shell, final Sequel seql) {
            this.origin = shell;
            this.sequel = seql;
        }
        /**
         * {@inheritDoc}
         * @checkstyle ParameterNumber (5 lines)
         */
        @Override
        public int exec(final String command, final InputStream stdin,
            final OutputStream stdout, final OutputStream stderr)
            throws IOException {
            return this.origin.exec(command, stdin, stdout, stderr);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws IOException {
            try {
                this.sequel.exec(this.origin);
            } finally {
                this.origin.close();
            }
        }

    }

}
