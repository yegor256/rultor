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
package com.rultor.scm;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Time;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * SCM Commit.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Commit {

    /**
     * Unique identifier.
     * @return Identifier of it
     * @throws IOException If fails
     */
    @NotNull(message = "name of commit is never NULL")
    String name() throws IOException;

    /**
     * Date of the commit.
     * @return Date
     * @throws IOException If fails
     */
    @NotNull(message = "time of commit is never NULL")
    Time time() throws IOException;

    /**
     * Author of the commit.
     * @return Author
     * @throws IOException If fails
     */
    @NotNull(message = "author of commit is never NULL")
    String author() throws IOException;

    /**
     * Simple implementation.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "label", "when", "who" })
    @Loggable(Loggable.DEBUG)
    final class Simple implements Commit {
        /**
         * Name of commit.
         */
        private final transient String label;
        /**
         * Date of commit.
         */
        private final transient Time when;
        /**
         * Author of commit.
         */
        private final transient String who;
        /**
         * Public ctor.
         * @param name Name of it
         * @param date When it happened
         * @param author Author of commit
         */
        public Simple(@NotNull final String name, @NotNull final Time date,
            @NotNull final String author) {
            this.label = name;
            this.when = date;
            this.who = author;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String name() throws IOException {
            return this.label;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Time time() throws IOException {
            return this.when;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String author() throws IOException {
            return this.who;
        }
    }

}
