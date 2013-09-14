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
package com.rultor.users.mongo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Pulses;
import com.rultor.spi.Query;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Query in Mongo stand.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "mongo", "mandatory", "optional" })
@Loggable(Loggable.DEBUG)
final class MongoQuery implements Query {

    /**
     * Mongo.
     */
    private final transient Mongo mongo;

    /**
     * Mandatory predicate.
     */
    private final transient Predicate mandatory;

    /**
     * Optional.
     */
    private final transient Predicate optional;

    /**
     * Public ctor.
     * @param mng Mongo
     * @param mnd Mandatory
     * @param opt Optional
     */
    protected MongoQuery(final Mongo mng, final Predicate mnd,
        final Predicate opt) {
        this.mongo = mng;
        this.mandatory = mnd;
        this.optional = opt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query withTag(final String name) {
        return new MongoQuery(
            this.mongo, this.mandatory,
            new Predicate.And(this.optional, new Predicate.WithTag(name))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pulses fetch() {
        return new MongoPulses(this.mongo, this.mandatory, this.optional);
    }

}
