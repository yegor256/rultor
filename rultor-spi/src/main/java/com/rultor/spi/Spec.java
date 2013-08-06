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
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Unit specification.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Spec {

    /**
     * Convert it to a human readable form.
     * @return The text
     */
    @NotNull(message = "spec text is never NULL")
    String asText();

    /**
     * Simple.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "text")
    @Loggable(Loggable.DEBUG)
    final class Simple implements Spec {
        /**
         * The text.
         */
        private final transient String text;
        /**
         * Public ctor.
         */
        public Simple() {
            this("com.rultor.base.Empty()");
        }
        /**
         * Public ctor.
         * @param spec The text
         */
        public Simple(@NotNull(message = "spec can't be NULL")
            final String spec) {
            this.text = spec;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String asText() {
            return this.text;
        }
    }

    /**
     * Strict spec.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "spec")
    @Loggable(Loggable.DEBUG)
    final class Strict implements Spec {
        /**
         * Simple spec that passed all quality controls.
         */
        private final transient Spec spec;
        /**
         * Public ctor.
         * @param text The text
         * @param repo Repo
         * @param user User
         * @param users Users
         * @param work Work we're in
         * @param type Type expected
         * @throws SpecException If fails
         */
        public Strict(
            @NotNull(message = "spec can't be NULL") final String text,
            @NotNull(message = "repo can't be NULL") final Repo repo,
            @NotNull(message = "user can't be NULL") final User user,
            @NotNull(message = "users can't be NULL") final Users users,
            @NotNull(message = "work can't be NULL") final Work work,
            @NotNull(message = "type can't be NULL") final Class<?> type)
            throws SpecException {
            final Spec temp = new Spec.Simple(text);
            final Variable<?> var = new Repo.Cached(repo, user, temp).get();
            if (var.arguments().isEmpty()) {
                final Object object = var.instantiate(
                    users, new Arguments(work)
                );
                try {
                    object.toString();
                } catch (SecurityException ex) {
                    throw new SpecException(ex);
                }
                if (!type.isAssignableFrom(object.getClass())) {
                    throw new SpecException(
                        String.format(
                            "%s expected while %s provided",
                            type.getName(),
                            object.getClass().getName()
                        )
                    );
                }
            }
            this.spec = new Spec.Simple(var.asText());
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String asText() {
            return this.spec.asText();
        }
    }

}
