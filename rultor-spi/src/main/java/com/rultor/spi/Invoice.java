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
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.CharUtils;

/**
 * One invoice.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Invoice {

    /**
     * Date of the invoice.
     * @return The date
     */
    @NotNull(message = "date of invoice is never NULL")
    Time date();

    /**
     * Amount of it.
     * @return The amount
     */
    @NotNull(message = "amount of invoice is never NULL")
    Dollars amount();

    /**
     * Details in text.
     * @return Text of the invoice
     */
    @NotNull(message = "text of invoices is never NULL")
    String text();

    /**
     * Composed of expenses.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = { "when", "expenses" })
    final class Composed implements Invoice {
        /**
         * Date of the invoice.
         */
        private final transient Time when = new Time();
        /**
         * Expenses included.
         */
        private final transient Expense[] expenses;
        /**
         * Public ctor.
         * @param exps Expenses
         */
        public Composed(final Collection<Expense> exps) {
            this.expenses = exps.toArray(new Expense[exps.size()]);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Time date() {
            return this.when;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Dollars amount() {
            long points = 0;
            for (Expense exp : this.expenses) {
                points += exp.dollars().points();
            }
            return new Dollars(points);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String text() {
            final StringBuilder text = new StringBuilder();
            for (Expense exp : this.expenses) {
                text.append(exp.details())
                    .append(' ')
                    .append(exp.dollars())
                    .append(CharUtils.LF);
            }
            return text.toString();
        }
    }

    /**
     * Unique code of invoice.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = "invoice")
    final class Code {
        /**
         * Original invoice.
         */
        private final transient Invoice invoice;
        /**
         * Public ctor.
         * @param inv Invoice to use
         */
        public Code(final Invoice inv) {
            this.invoice = inv;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format(
                "%s %d %s",
                this.invoice.date(),
                this.invoice.amount().points(),
                DigestUtils.md5Hex(this.invoice.text())
            );
        }
        /**
         * Get date of this code.
         * @param code The code
         * @return Date found inside
         */
        public static Time dateOf(final String code) {
            final String[] parts = code.split(" ");
            return new Time(parts[0]);
        }
    }

}
