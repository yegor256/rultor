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

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
     * Make an instance from a spec.
     * @param user Owner of this spec
     * @param spec Spect
     * @return The instance
     * @throws SpecException If can't instantiate
     */
    @NotNull
    Variable<?> make(@NotNull User user, @NotNull Spec spec)
        throws SpecException;

    /**
     * Cached repo.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "repo", "user", "spec" })
    @Loggable(Loggable.DEBUG)
    final class Cached {
        /**
         * Original repo.
         */
        private final transient Repo repo;
        /**
         * User.
         */
        private final transient User user;
        /**
         * Spec.
         */
        private final transient Spec spec;
        /**
         * Public ctor.
         * @param rep Repo
         * @param usr User
         * @param spc Spec
         */
        public Cached(@NotNull final Repo rep, @NotNull final User usr,
            @NotNull final Spec spc) {
            this.repo = rep;
            this.user = usr;
            this.spec = spc;
        }
        /**
         * Get an object.
         * @return The object or exception if fails
         * @throws SpecException If fails
         */
        @Cacheable(lifetime = 1, unit = TimeUnit.HOURS)
        public Variable<?> get() throws SpecException {
            return this.repo.make(this.user, this.spec);
        }
    }

}
