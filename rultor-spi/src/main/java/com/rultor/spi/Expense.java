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

/**
 * Expense to register in {@link Work}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Expense {

    /**
     * Details.
     * @return Details
     */
    @NotNull(message = "details of expense is never NULL")
    String details();

    /**
     * Dollar amount in points where 1 USD equals
     * to 1,000,000 points (a million).
     * @return The amount
     */
    @NotNull(message = "amount of transaction is never NULL")
    long points();

    /**
     * Simple implementation.
     */
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = { "text", "amount" })
    @Immutable
    final class Simple implements Expense {
        /**
         * Details.
         */
        private final transient String text;
        /**
         * Amount of it.
         */
        private final transient long amount;
        /**
         * Public ctor.
         * @param details Details
         * @param points Amount
         */
        public Simple(
            @NotNull(message = "details can't be NULL") final String details,
            final long points) {
            this.text = details;
            this.amount = points;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "details of transaction is never NULL")
        public String details() {
            return this.text;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "amount of transaction is never NULL")
        public long points() {
            return this.amount;
        }
    }

}
