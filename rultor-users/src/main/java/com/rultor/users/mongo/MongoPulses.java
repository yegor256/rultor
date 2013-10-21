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
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pageable;
import com.rultor.spi.Pulse;
import com.rultor.spi.Pulses;
import com.rultor.spi.Query;
import com.rultor.spi.Stand;
import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Pulses in Mongo stand.
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
final class MongoPulses implements Pulses {

    /**
     * Mongo container.
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
     * @param mng Mongo container
     * @param stand Original
     */
    protected MongoPulses(final Mongo mng, final Stand stand) {
        this(
            mng,
            new Predicate.InStand(stand.name()),
            new Predicate.Any()
        );
    }

    /**
     * Private ctor.
     * @param mng Mongo
     * @param mnd Mandatory predicate
     * @param opt Optional predicate
     */
    protected MongoPulses(final Mongo mng, final Predicate mnd,
        final Predicate opt) {
        this.mongo = mng;
        this.mandatory = mnd;
        this.optional = opt;
    }

    @Override
    public Iterator<Pulse> iterator() {
        final DBCursor cursor = this.collection().find(
            new Predicate.And(this.mandatory, this.optional).query(),
            new BasicDBObject()
                .append(MongoStand.ATTR_COORDS, true)
                .append(MongoStand.ATTR_STAND, true)
                .append(MongoStand.ATTR_TAGS, true)
        );
        cursor.sort(new BasicDBObject(MongoStand.ATTR_UPDATED, -1));
        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() {
                    cursor.close();
                }
            },
            TimeUnit.MINUTES.toMillis(1)
        );
        // @checkstyle AnonInnerLength (50 lines)
        return new Iterator<Pulse>() {
            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }
            @Override
            public Pulse next() {
                return new MongoPulse(MongoPulses.this.mongo, cursor.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Pageable<Pulse, Coordinates> tail(final Coordinates top) {
        return new MongoPulses(
            this.mongo,
            new Predicate.And(this.mandatory, new Predicate.Tail(top)),
            this.optional
        );
    }

    @Override
    public Query query() {
        return new MongoQuery(this.mongo, this.mandatory, this.optional);
    }

    /**
     * Collection.
     * @return Mongo collection
     */
    private DBCollection collection() {
        try {
            return this.mongo.get().getCollection(MongoStand.TABLE);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
