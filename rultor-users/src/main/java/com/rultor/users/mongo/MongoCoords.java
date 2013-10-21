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
import com.jcabi.urn.URN;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rultor.spi.Coordinates;
import com.rultor.tools.Time;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Coordinates in Mongo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "urn", "name", "time" })
@Loggable(Loggable.DEBUG)
final class MongoCoords implements Coordinates {

    /**
     * MongoDB table column.
     */
    public static final String ATTR_RULE = "rule";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_OWNER = "owner";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_SCHEDULED = "scheduled";

    /**
     * Owner.
     */
    private final transient URN urn;

    /**
     * Name of the rule.
     */
    private final transient String name;

    /**
     * Scheduled time.
     */
    private final transient Time time;

    /**
     * Public ctor.
     * @param object The object
     */
    protected MongoCoords(final DBObject object) {
        this(
            URN.create(object.get(MongoCoords.ATTR_OWNER).toString()),
            object.get(MongoCoords.ATTR_RULE).toString(),
            new Time(object.get(MongoCoords.ATTR_SCHEDULED).toString())
        );
    }

    /**
     * Public ctor.
     * @param coords Other coordinates
     */
    protected MongoCoords(final Coordinates coords) {
        this(coords.owner(), coords.rule(), coords.scheduled());
    }

    /**
     * Public ctor.
     * @param owner Owner
     * @param rule Rule
     * @param scheduled Scheduled
     */
    private MongoCoords(final URN owner, final String rule,
        final Time scheduled) {
        this.urn = owner;
        this.name = rule;
        this.time = scheduled;
    }

    /**
     * Make Mongo DBObject out of it.
     * @return Object
     */
    public DBObject asObject()  {
        return new BasicDBObject()
            .append(MongoCoords.ATTR_OWNER, this.owner().toString())
            .append(MongoCoords.ATTR_RULE, this.rule())
            .append(MongoCoords.ATTR_SCHEDULED, this.scheduled().toString());
    }

    @Override
    public Time scheduled() {
        return this.time;
    }

    @Override
    public URN owner() {
        return this.urn;
    }

    @Override
    public String rule() {
        return this.name;
    }

    @Override
    public int compareTo(final Coordinates coords) {
        return this.time.compareTo(coords.scheduled());
    }

}
