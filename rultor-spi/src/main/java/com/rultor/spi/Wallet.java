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
import com.rultor.tools.Dollars;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Wallet.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Wallet {

    /**
     * Charge some money.
     * @param details Description of operation
     * @param amount Amount of money to charge
     */
    void charge(
        @NotNull(message = "details can't be NULL") String details,
        @NotNull(message = "amount can't be NULL") Dollars amount);

    /**
     * Delegate to another user/rule.
     * @param urn URN of another user
     * @param rule Name of the rule
     * @return New wallet
     * @throws Wallet.NotEnoughFundsException If not enough
     */
    Wallet delegate(
        @NotNull(message = "URN can't be NULL") URN urn,
        @NotNull(message = "rule name can't be NULL") String rule)
        throws Wallet.NotEnoughFundsException;

    /**
     * When not enough funds in the wallet.
     */
    final class NotEnoughFundsException extends Exception {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x65c4cafe3f528092L;
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public NotEnoughFundsException(final String cause) {
            super(cause);
        }
        /**
         * Public ctor.
         * @param cause Cause of it
         */
        public NotEnoughFundsException(final Exception cause) {
            super(cause);
        }
    }

    /**
     * Empty wallet doing nothing.
     */
    @Immutable
    @EqualsAndHashCode
    @Loggable(Loggable.INFO)
    final class Empty implements Wallet {
        /**
         * {@inheritDoc}
         */
        @Override
        public void charge(final String details, final Dollars amount) {
            assert details != null;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Wallet delegate(final URN urn, final String rule) {
            return this;
        }
    }

}
