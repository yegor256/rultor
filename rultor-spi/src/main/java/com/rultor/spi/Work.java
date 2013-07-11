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
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Work to do.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Work {

    /**
     * When started, in milliseconds.
     * @return Milliseconds
     */
    @NotNull(message = "time is never NULL")
    Time started();

    /**
     * Owner of this work.
     * @return The owner
     */
    @NotNull(message = "URN of owner is never NULL")
    URN owner();

    /**
     * Name of the work (unique for the user).
     * @return The unit
     */
    @NotNull(message = "unit name is never NULL")
    String unit();

    /**
     * Spec to run.
     * @return The spec
     */
    @NotNull(message = "spec is never NULL")
    Spec spec();

    /**
     * No work at all.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    final class None implements Work {
        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public Time started() {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public URN owner() {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public String unit() {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public Spec spec() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Simple implementation.
     */
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = { "time", "urn", "label" })
    @Immutable
    final class Simple implements Work {
        /**
         * When started.
         */
        private final transient Time time;
        /**
         * Owner of it.
         */
        private final transient URN urn;
        /**
         * Name of it.
         */
        private final transient String label;
        /**
         * Spec of it.
         */
        private final transient Spec desc;
        /**
         * Public ctor.
         */
        public Simple() {
            this(URN.create("urn:facebook:1"), "test-unit");
        }
        /**
         * Public ctor.
         * @param owner Owner
         * @param name Name
         */
        public Simple(final URN owner, final String name) {
            this(owner, name, new Time());
        }
        /**
         * Public ctor.
         * @param owner Owner
         * @param name Name
         * @param when Time
         */
        public Simple(final URN owner, final String name, final Time when) {
            this(owner, name, new Spec.Simple(), when);
        }
        /**
         * Public ctor.
         * @param owner Owner
         * @param name Name
         * @param spec Spec
         */
        public Simple(final URN owner, final String name, final Spec spec) {
            this(owner, name, spec, new Time());
        }
        /**
         * Public ctor.
         * @param owner Owner
         * @param name Name
         * @param spec Spec
         * @param when When it should start
         * @checkstyle ParameterNumber (5 lines)
         */
        public Simple(@NotNull(message = "owner can't be NULL") final URN owner,
            @NotNull(message = "unit name can't be NULL") final String name,
            @NotNull(message = "spec can't be NULL") final Spec spec,
            @NotNull(message = "time can't be NULL") final Time when) {
            this.urn = owner;
            this.label = name;
            this.desc = spec;
            this.time = when;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format(
                "at %s in %s for %s",
                this.time,
                this.label,
                this.urn
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "time of work is never NULL")
        public Time started() {
            return this.time;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "URN of owner of work is never NULL")
        public URN owner() {
            return this.urn;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "unit name of work is never NULL")
        public String unit() {
            return this.label;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "spec of work is never NULL")
        public Spec spec() {
            return this.desc;
        }
    }

}
