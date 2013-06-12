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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import javax.validation.constraints.NotNull;

/**
 * Repository of classes.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Repo {

    /**
     * Invalid syntax of spec.
     */
    final class InvalidSyntaxException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x65740abe34528092L;
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public InvalidSyntaxException(final String cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public InvalidSyntaxException(final Exception cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param cause Cause of it
         * @param origin Original exception
         */
        public InvalidSyntaxException(final String cause,
            final Exception origin) {
            super(cause, origin);
        }
    }

    /**
     * Spec can't be instantiated.
     */
    final class InstantiationException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x65f40afe34528092L;
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public InstantiationException(final String cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public InstantiationException(final Exception cause) {
            super(cause);
        }
    }

    /**
     * Make a spec from text.
     * @param text Text
     * @return The spec
     * @throws Repo.InvalidSyntaxException If incorrect syntax
     */
    Spec make(@NotNull String text) throws Repo.InvalidSyntaxException;

    /**
     * Make an instance from a spec.
     * @param spec Spect
     * @return The instance
     * @throws Repo.InstantiationException If can't instantiate
     */
    Instance make(@NotNull Spec spec) throws Repo.InstantiationException;

}
