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
import com.jcabi.immutable.Array;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rultor.spi.Coordinates;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Predicate for MongoDB search.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
interface Predicate {

    /**
     * Get query DBObject.
     * @return DBObject for query
     */
    DBObject query();

    /**
     * Any.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Any implements Predicate {
        @Override
        public DBObject query() {
            return new BasicDBObject();
        }
    }

    /**
     * AND predicate.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "left", "right" })
    @Loggable(Loggable.DEBUG)
    final class And implements Predicate {
        /**
         * Left predicate.
         */
        private final transient Predicate left;
        /**
         * Right predicate.
         */
        private final transient Predicate right;
        /**
         * Public ctor.
         * @param lft Left
         * @param rght Right
         */
        public And(final Predicate lft, final Predicate rght) {
            this.left = lft;
            this.right = rght;
        }
        @Override
        public DBObject query() {
            final DBObject query = new BasicDBObject();
            query.putAll(this.left.query());
            query.putAll(this.right.query());
            return query;
        }
    }

    /**
     * TAIL predicate.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "head")
    @Loggable(Loggable.DEBUG)
    final class Tail implements Predicate {
        /**
         * Coords to start with.
         */
        private final transient Coordinates head;
        /**
         * Public ctor.
         * @param coords Head
         */
        public Tail(final Coordinates coords) {
            this.head = coords;
        }
        @Override
        public DBObject query() {
            return new BasicDBObject().append(
                MongoStand.ATTR_COORDS, new MongoCoords(this.head).asObject()
            );
        }
    }

    /**
     * In stand predicate.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "name")
    @Loggable(Loggable.DEBUG)
    final class InStand implements Predicate {
        /**
         * Name of the stand.
         */
        private final transient String name;
        /**
         * Public ctor.
         * @param stand Name of the stand
         */
        public InStand(final String stand) {
            this.name = stand;
        }
        @Override
        public DBObject query() {
            return new BasicDBObject().append(
                MongoStand.ATTR_STAND, this.name
            );
        }
    }

    /**
     * In stands predicate.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "names")
    @Loggable(Loggable.DEBUG)
    final class InStands implements Predicate {
        /**
         * Names of the stands.
         */
        private final transient Array<String> names;
        /**
         * Public ctor.
         * @param stands Name of the stand
         */
        public InStands(final Collection<String> stands) {
            this.names = new Array<String>(stands);
        }
        @Override
        public DBObject query() {
            return new BasicDBObject().append(
                MongoStand.ATTR_STAND,
                new BasicDBObject().append("$in", this.names)
            );
        }
    }

    /**
     * With tag predicate.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "label")
    @Loggable(Loggable.DEBUG)
    final class WithTag implements Predicate {
        /**
         * Label of the tag.
         */
        private final transient String label;
        /**
         * Public ctor.
         * @param txt Label of the tag
         */
        public WithTag(final String txt) {
            this.label = txt;
        }
        @Override
        public DBObject query() {
            return new BasicDBObject().append(
                MongoStand.ATTR_TAGS,
                new BasicDBObject().append(
                    "$elemMatch",
                    new BasicDBObject().append(
                        MongoTag.ATTR_LABEL, this.label
                    )
                )
            );
        }
    }

}
