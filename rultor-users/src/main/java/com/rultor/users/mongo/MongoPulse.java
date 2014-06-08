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
import com.jcabi.immutable.ArrayMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pulse;
import com.rultor.spi.Tag;
import com.rultor.spi.Tags;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Single pulse in Mongo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "map")
@Loggable(Loggable.DEBUG)
final class MongoPulse implements Pulse {

    /**
     * Mongo container.
     */
    private final transient Mongo mongo;

    /**
     * Mongo data map.
     */
    private final transient ArrayMap<String, Object> map;

    /**
     * Public ctor.
     * @param mng Mongo
     * @param object The object
     */
    @SuppressWarnings("unchecked")
    protected MongoPulse(final Mongo mng, final DBObject object) {
        this.mongo = mng;
        this.map = new ArrayMap<String, Object>(object.toMap());
    }

    @Override
    public String xembly() throws IOException {
        final DBObject object = this.collection().findOne(
            // @checkstyle MultipleStringLiterals (1 line)
            new BasicDBObject().append("_id", this.map.get("_id")),
            new BasicDBObject()
                .append(MongoStand.ATTR_XEMBLY, true)
                .append(MongoStand.ATTR_UPDATED, true)
        );
        final String xembly = object.get(MongoStand.ATTR_XEMBLY).toString();
        return new StringBuilder()
            .append(MongoStand.decode(xembly))
            .append("XPATH '/snapshot'; ADDIF 'updated';")
            .append("SET '")
            .append(object.get(MongoStand.ATTR_UPDATED))
            .append("';")
            .toString();
    }

    @Override
    public InputStream stream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Coordinates coordinates() {
        return new MongoCoords(
            DBObject.class.cast(this.map.get(MongoStand.ATTR_COORDS))
        );
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Tags tags() {
        final Collection<?> objects =
            Collection.class.cast(this.map.get(MongoStand.ATTR_TAGS));
        final Collection<Tag> tags = new ArrayList<Tag>(objects.size());
        for (final Object object : objects) {
            tags.add(new MongoTag(DBObject.class.cast(object)));
        }
        return new Tags.Simple(tags);
    }

    @Override
    public String stand() {
        return this.map.get(MongoStand.ATTR_STAND).toString();
    }

    /**
     * Collection.
     * @return Mongo collection
     */
    private DBCollection collection() {
        try {
            return this.mongo.get().getCollection(MongoStand.TABLE);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
