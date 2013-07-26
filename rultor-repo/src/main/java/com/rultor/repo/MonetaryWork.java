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
package com.rultor.repo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.urn.URN;
import com.rultor.spi.Receipt;
import com.rultor.spi.Spec;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * Work that is capable of tracking expenses.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "users", "origin", "client", "user", "name" })
@Loggable(Loggable.DEBUG)
final class MonetaryWork implements Work {

    /**
     * Users.
     */
    private final transient Users users;

    /**
     * Original work.
     */
    private final transient Work origin;

    /**
     * Client of the unit (who is using the unit).
     */
    private final transient URN client;

    /**
     * Owner of the unit (who provides the unit).
     */
    private final transient URN user;

    /**
     * The name.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param usrs Users
     * @param wrk Origin work
     * @param clnt URN of the client
     * @param owner URN of the owner
     * @param unit Name of the unit
     * @checkstyle ParameterNumber (4 lines)
     */
    protected MonetaryWork(final Users usrs, final Work wrk, final URN clnt,
        final URN owner, final String unit) {
        this.users = usrs;
        this.origin = wrk;
        this.client = clnt;
        this.user = owner;
        this.name = unit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.origin.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Time started() {
        return this.origin.started();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN owner() {
        return this.origin.owner();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String unit() {
        return this.origin.unit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spec spec() {
        return this.origin.spec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RetryOnFailure
    public void charge(final String details, final Dollars amount) {
        Validate.isTrue(
            amount.points() > 0,
            "charge amount can be positive only, %s provided", amount
        );
        this.users.charge(
            new Receipt.Simple(
                new Time(),
                this.client,
                this.user,
                String.format("%s: %s", this.unit(), details),
                amount,
                this.name
            )
        );
    }
}
