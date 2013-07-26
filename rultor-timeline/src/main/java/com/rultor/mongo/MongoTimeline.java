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
package com.rultor.mongo;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.immutable.ArrayMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.rultor.timeline.Event;
import com.rultor.timeline.Permissions;
import com.rultor.timeline.Product;
import com.rultor.timeline.Tag;
import com.rultor.timeline.Timeline;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * Timeline in Mongo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "mongo", "attrs" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class MongoTimeline implements Timeline {

    /**
     * Mongo attribute.
     */
    public static final String ATTR_NAME = "name";

    /**
     * Mongo attribute.
     */
    public static final String ATTR_KEY = "key";

    /**
     * Mongo attribute.
     */
    public static final String ATTR_OWNER = "owner";

    /**
     * Mongo container.
     */
    private final transient Mongo mongo;

    /**
     * Data from DB.
     */
    private final transient ArrayMap<String, Object> attrs;

    /**
     * Public ctor.
     * @param mng Mongo container
     * @param object Object from DB
     */
    @SuppressWarnings("unchecked")
    public MongoTimeline(final Mongo mng, final DBObject object) {
        this.mongo = mng;
        this.attrs = new ArrayMap<String, Object>(object.toMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.attrs.get(MongoTimeline.ATTR_NAME).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Permissions permissions() {
        return new MongoPermissions(this.mongo, this.attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Event post(final String text, final Collection<Tag> tags,
        final Collection<Product> products) {
        final DBObject object = new BasicDBObject(
            new ImmutableMap.Builder<String, Object>()
                .put(MongoEvent.ATTR_TIMELINE, this.name())
                .put(MongoEvent.ATTR_TEXT, text)
                .put(MongoEvent.ATTR_TIME, System.currentTimeMillis())
                .put(MongoEvent.ATTR_TAGS, MongoTimeline.tags(tags))
                .put(MongoEvent.ATTR_PRODS, MongoTimeline.products(products))
                .build()
        );
        final WriteResult result = this.ecol().insert(object);
        Validate.isTrue(
            result.getLastError().ok(),
            "failed to create new event `%s`: %s",
            text, result.getLastError().getErrorMessage()
        );
        return new MongoEvent(object.toMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Event> events(final Time head) {
        // @checkstyle AnonInnerLength (50 lines)
        return new Iterable<Event>() {
            @Override
            public Iterator<Event> iterator() {
                final DBCursor cursor = MongoTimeline.this.ecol().find(
                    new BasicDBObject(
                        MongoEvent.ATTR_TIME,
                        new BasicDBObject("$lte", head.millis())
                    ).append(
                        MongoEvent.ATTR_TIMELINE, MongoTimeline.this.name()
                    )
                );
                cursor.sort(new BasicDBObject(MongoEvent.ATTR_TIME, -1));
                return new Iterator<Event>() {
                    @Override
                    public boolean hasNext() {
                        return cursor.hasNext();
                    }
                    @Override
                    @SuppressWarnings("unchecked")
                    public Event next() {
                        return new MongoEvent(cursor.next().toMap());
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Product> products() {
        return new ArrayList<Product>(0);
    }

    /**
     * Collection.
     * @return Mongo collection with events
     */
    private DBCollection ecol() {
        try {
            return this.mongo.get().getCollection("events");
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Collection of tags into DB object.
     * @param tags Collection of them
     * @return Array of objects
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static DBObject[] tags(final Collection<Tag> tags) {
        final DBObject[] objects = new DBObject[tags.size()];
        int idx = 0;
        for (Tag tag : tags) {
            Validate.isTrue(
                tag.label().length() <= Tv.FORTY,
                "tag label '%s' is too long, should be less than 40",
                tag.label()
            );
            objects[idx] = new BasicDBObject(
                new ImmutableMap.Builder<String, Object>()
                    .put(MongoTag.ATTR_LABEL, tag.label())
                    .put(MongoTag.ATTR_LEVEL, tag.level().toString())
                    .build()
            );
            ++idx;
        }
        return objects;
    }

    /**
     * Collection of products into DB object.
     * @param products Collection of them
     * @return Array of objects
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static DBObject[] products(final Collection<Product> products) {
        final DBObject[] objects = new DBObject[products.size()];
        int idx = 0;
        for (Product product : products) {
            Validate.isTrue(
                product.name().length() <= Tv.HUNDRED,
                "product name '%s' is too long, should be less than 100",
                product.name()
            );
            objects[idx] = new BasicDBObject(
                new ImmutableMap.Builder<String, Object>()
                    .put(MongoProduct.ATTR_NAME, product.name())
                    .put(MongoProduct.ATTR_MARKDOWN, product.markdown())
                    .build()
            );
            ++idx;
        }
        return objects;
    }

}
