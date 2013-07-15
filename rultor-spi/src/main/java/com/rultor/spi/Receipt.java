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

/**
 * Receipt to register in {@link Work}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Receipt {

    /**
     * When it happened.
     * @return The date
     */
    @NotNull(message = "date or receipt is never NULL")
    Time date();

    /**
     * Who is paying.
     * @return The URN
     */
    @NotNull(message = "payer or receipt is never NULL")
    URN payer();

    /**
     * Who is receiving.
     * @return The URN
     */
    @NotNull(message = "beneficiary or receipt is never NULL")
    URN beneficiary();

    /**
     * Details.
     * @return Details
     */
    @NotNull(message = "details of receipt is never NULL")
    String details();

    /**
     * Dollar amount.
     * @return The amount
     */
    @NotNull(message = "amount of receipt is never NULL")
    Dollars dollars();

    /**
     * Unit that this receipt is for.
     * @return Unit name
     */
    @NotNull(message = "unit of receipt is never NULL")
    String unit();

    /**
     * Simple implementation.
     */
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = { "text", "amount" })
    @Immutable
    final class Simple implements Receipt {
        /**
         * When did it happen.
         */
        private final transient Time when;
        /**
         * Who is paying.
         */
        private final transient URN pyr;
        /**
         * Who is receiving.
         */
        private final transient URN rcv;
        /**
         * Details.
         */
        private final transient String text;
        /**
         * Amount of it.
         */
        private final transient Dollars amount;
        /**
         * Unit name of it.
         */
        private final transient String subject;
        /**
         * Public ctor.
         * @param time Time of receipt
         * @param payer Payer
         * @param beneficiary Receiver
         * @param details Details
         * @param points Amount
         * @param unit Name of the unit
         * @checkstyle ParameterNumber (10 lines)
         */
        public Simple(
            @NotNull(message = "date can't be NULL") final Time time,
            @NotNull(message = "payer can't be NULL") final URN payer,
            @NotNull(message = "receiver can't be NULL") final URN beneficiary,
            @NotNull(message = "details can't be NULL") final String details,
            @NotNull(message = "points can't be NULL") final Dollars points,
            @NotNull(message = "unit can't be NULL") final String unit) {
            this.when = time;
            this.pyr = payer;
            this.rcv = beneficiary;
            this.text = details;
            this.amount = points;
            this.subject = unit;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "details of receipt is never NULL")
        public String details() {
            return this.text;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "amount of receipt is never NULL")
        public Dollars dollars() {
            return this.amount;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "date of receipt is never NULL")
        public Time date() {
            return this.when;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "payer of receipt is never NULL")
        public URN payer() {
            return this.pyr;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "beneficiary of receipt is never NULL")
        public URN beneficiary() {
            return this.rcv;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "unit of receipt is never NULL")
        public String unit() {
            return this.subject;
        }
    }

}
