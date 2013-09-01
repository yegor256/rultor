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
 * User.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface User {

    /**
     * His URN.
     * @return URN
     */
    @NotNull(message = "URN of user is never NULL")
    URN urn();

    /**
     * Get user's account.
     * @return All receipts
     */
    @NotNull(message = "account is never NULL")
    Account account();

    /**
     * Names of all his rules.
     * @return Collection of rules
     */
    @NotNull(message = "set of rules of user is never NULL")
    Rules rules();

    /**
     * Names of all his stands.
     * @return Collection of stand names
     */
    @NotNull(message = "set of stands of user is never NULL")
    Stands stands();

    /**
     * Nobody.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode
    final class Nobody implements User {
        @Override
        public URN urn() {
            return URN.create("urn:test:0");
        }
        @Override
        public Account account() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Rules rules() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Stands stands() {
            throw new UnsupportedOperationException();
        }
    }

}
