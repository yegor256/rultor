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
import com.mongodb.DBObject;
import com.rultor.spi.Pageable;
import com.rultor.spi.Pulse;
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
@EqualsAndHashCode(of = { "mongo", "origin" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
final class MongoPulses implements Pageable<Pulse, String> {

    /**
     * Mongo container.
     */
    private final transient Mongo mongo;

    /**
     * Original stand.
     */
    private final transient Stand origin;

    /**
     * Head of the list.
     */
    private final transient String head;

    /**
     * Public ctor.
     * @param mng Mongo container
     * @param stnd Original
     */
    protected MongoPulses(final Mongo mng, final Stand stnd) {
        this(mng, stnd, "");
    }

    /**
     * Private ctor.
     * @param mng Mongo container
     * @param stnd Original
     * @param top Head of the list
     */
    private MongoPulses(final Mongo mng, final Stand stnd, final String top) {
        this.mongo = mng;
        this.origin = stnd;
        this.head = top;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Pulse> iterator() {
        final DBCursor cursor = this.collection().find(this.query());
        cursor.sort(new BasicDBObject(MongoStand.ATTR_UPDATED, -1));
        this.close(cursor);
        // @checkstyle AnonInnerLength (50 lines)
        return new Iterator<Pulse>() {
            @Override
            public boolean hasNext() {
                return cursor.hasNext();
            }
            @Override
            public Pulse next() {
                return new MongoPulse(cursor.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Pulse, String> tail(final String top) {
        return new MongoPulses(this.mongo, this.origin, top);
    }

    /**
     * Build query.
     * @return Query
     */
    private DBObject query() {
        final BasicDBObject query = new BasicDBObject()
            .append(MongoStand.ATTR_STAND, this.origin.name());
        if (!this.head.isEmpty()) {
            query.append(
                MongoStand.ATTR_PULSE,
                new BasicDBObject("$lte", this.head)
            );
        }
        return query;
    }

    /**
     * Close it later.
     * @param cursor The cursor
     */
    private void close(final DBCursor cursor) {
        new Timer().schedule(
            new TimerTask() {
                @Override
                public void run() {
                    cursor.close();
                }
            },
            TimeUnit.MINUTES.toMillis(1)
        );
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
