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
package com.rultor.users.pgsql;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Unit;
import com.rultor.spi.Units;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Units with extra features from PostgreSQL.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "client", "origin" })
@Loggable(Loggable.DEBUG)
final class PgUnits implements Units {

    /**
     * PostgreSQL client.
     */
    private final transient PgClient client;

    /**
     * Original units.
     */
    private final transient Units origin;

    /**
     * Public ctor.
     * @param clnt Client
     * @param unts Units
     */
    protected PgUnits(final PgClient clnt, final Units unts) {
        this.client = clnt;
        this.origin = unts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final String name) {
        return this.origin.contains(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Unit get(final String name) {
        return new PgUnit(this.client, this.origin.get(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(final String name) {
        this.origin.remove(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final String name) {
        this.origin.create(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Unit> iterator() {
        final Iterator<Unit> iter = this.origin.iterator();
        return new Iterator<Unit>() {
            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }
            @Override
            public Unit next() {
                return new PgUnit(PgUnits.this.client, iter.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
