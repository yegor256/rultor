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
import com.jcabi.urn.URN;
import javax.validation.constraints.NotNull;

/**
 * Rule.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Rule {

    /**
     * Get its name.
     * @return Name of it
     */
    @NotNull(message = "name of rule is never NULL")
    String name();

    /**
     * Save spec.
     * @param spec Spec to save
     * @param drain Spec of drain
     */
    void update(@NotNull(message = "spec can't be NULL") Spec spec,
        @NotNull(message = "drain can't be NULL") Spec drain);

    /**
     * Get spec.
     * @return Spec
     */
    @NotNull(message = "spec is never NULL")
    Spec spec();

    /**
     * Get drain spec.
     * @return Spec
     */
    @NotNull(message = "spec of drain is never NULL")
    Spec drain();

    /**
     * Mark it as failed.
     * @param desc Description of a failure
     */
    void failure(@NotNull(message = "desc can't be NULL") String desc);

    /**
     * Read description of failure or empty string if there is no failure.
     * @return Description of it or empty string
     */
    String failure();

    /**
     * Wallet of the rule.
     * @param work Which work for
     * @param taker Who is going to take money from my wallet?
     * @param rule What this money is for?
     * @return Wallet
     * @throws Wallet.NotEnoughFundsException If not enough funds
     * @checkstyle RedundantThrowsCheck (5 lines)
     */
    @NotNull(message = "wallet is never NULL")
    Wallet wallet(Coordinates work, URN taker, String rule)
        throws Wallet.NotEnoughFundsException;

}
