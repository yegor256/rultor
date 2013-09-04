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
package com.rultor.conveyer.audit;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.spi.Account;
import com.rultor.spi.Rules;
import com.rultor.spi.Stands;
import com.rultor.spi.User;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * User with audit features.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
final class AuditUser implements User {

    /**
     * Minimum threshold in points.
     */
    private static final long THRESHOLD = -Tv.FIVE * Tv.MILLION;

    /**
     * Original user.
     */
    private final transient User origin;

    /**
     * Public ctor.
     * @param user User
     */
    protected AuditUser(final User user) {
        this.origin = user;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN urn() {
        return this.origin.urn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Rules rules() {
        return new AuditRules(
            this.origin.rules(),
            this.account().balance().points() > AuditUser.THRESHOLD
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Stands stands() {
        return this.origin.stands();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Account account() {
        return this.origin.account();
    }

}
