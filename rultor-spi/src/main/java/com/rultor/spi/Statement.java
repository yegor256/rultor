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
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * One financial statement.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Statement {

    /**
     * Date of the statement.
     * @return The date
     */
    @NotNull(message = "date of statement is never NULL")
    Time date();

    /**
     * Amount of it.
     * @return The amount
     */
    @NotNull(message = "amount of statement is never NULL")
    Dollars amount();

    /**
     * Final balance.
     * @return The balance
     */
    @NotNull(message = "balance is never NULL")
    Dollars balance();

    /**
     * Details in text.
     * @return Text of the statement
     */
    @NotNull(message = "text of statements is never NULL")
    String details();

    /**
     * Simple implementation.
     */
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = { "when", "total", "end", "text" })
    @Immutable
    final class Simple implements Statement {
        /**
         * When did it happen.
         */
        private final transient Time when;
        /**
         * Total amount.
         */
        private final transient Dollars total;
        /**
         * Ending balance.
         */
        private final transient Dollars end;
        /**
         * Details.
         */
        private final transient String text;
        /**
         * Public ctor (balance it now known).
         * @param time Time of statement
         * @param amount Amount of it
         * @param details Details
         */
        public Simple(final Time time, final Dollars amount,
            final String details) {
            this(time, amount, new Dollars(0), details);
        }
        /**
         * Public ctor.
         * @param time Time of statement
         * @param amount Amount of it
         * @param balance Balance
         * @param details Details
         * @checkstyle ParameterNumber (10 lines)
         */
        public Simple(
            @NotNull(message = "date can't be NULL") final Time time,
            @NotNull(message = "amount can't be NULL") final Dollars amount,
            @NotNull(message = "balance can't be NULL") final Dollars balance,
            @NotNull(message = "details can't be NULL") final String details) {
            this.when = time;
            this.total = amount;
            this.end = balance;
            this.text = details;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "details of statement is never NULL")
        public String details() {
            return this.text;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "amount of statement is never NULL")
        public Dollars amount() {
            return this.total;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "balance of statement is never NULL")
        public Dollars balance() {
            return this.end;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "date of statement is never NULL")
        public Time date() {
            return this.when;
        }
    }

}
