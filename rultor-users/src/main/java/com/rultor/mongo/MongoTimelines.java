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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.rultor.timeline.Timeline;
import com.rultor.timeline.Timelines;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Timelines in Mongo.
 *
 * <pre>
 * find {
 *   owner: String,
 *   name: String,
 *   friends[]: String,
 *   key: String
 * };
 * events {
 *   time: Date,
 *   text: String,
 *   tags[]: {
 *     name: String,
 *     level: String
 *   },
 *   products[]: {
 *     name: String,
 *     markdown: String
 *   }
 * };
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "mongo")
@Loggable(Loggable.DEBUG)
public final class MongoTimelines implements Timelines {

    /**
     * Mongo container.
     */
    private final transient Mongo mongo;

    /**
     * Public ctor.
     * @param mng Mongo container
     */
    public MongoTimelines(final Mongo mng) {
        this.mongo = mng;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterable<Timeline> find(final URN owner) {
        final DBCursor cursor = this.collection().find(
            new BasicDBObject(MongoTimeline.ATTR_OWNER, owner.toString())
        );
        cursor.sort(new BasicDBObject(MongoTimeline.ATTR_NAME, 1));
        final Collection<Timeline> timelines = new LinkedList<Timeline>();
        try {
            while (cursor.hasNext()) {
                timelines.add(new MongoTimeline(this.mongo, cursor.next()));
            }
        } finally {
            cursor.close();
        }
        return timelines;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Timeline create(final URN owner, final String name)
        throws TimelineExistsException {
        final DBCursor cursor = this.collection().find(
            new BasicDBObject(MongoTimeline.ATTR_NAME, name)
        );
        try {
            if (cursor.hasNext()) {
                throw new TimelineExistsException(
                    String.format("timeline `%s` already exists", name)
                );
            }
        } finally {
            cursor.close();
        }
        final DBObject object = new BasicDBObject()
            .append(MongoTimeline.ATTR_OWNER, owner.toString())
            .append(MongoTimeline.ATTR_NAME, name)
            .append("friends", new String[0])
            .append(
                MongoTimeline.ATTR_KEY,
                RandomStringUtils.randomAlphanumeric(Tv.TWENTY)
            );
        final WriteResult result = this.collection().insert(object);
        Validate.isTrue(
            result.getLastError().ok(),
            "failed to create new timeline `%s`: %s",
            name, result.getLastError().getErrorMessage()
        );
        return new MongoTimeline(this.mongo, object);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Timeline get(final String name)
        throws Timelines.TimelineNotFoundException {
        final DBCursor cursor = this.collection().find(
            new BasicDBObject(MongoTimeline.ATTR_NAME, name)
        );
        try {
            if (!cursor.hasNext()) {
                throw new Timelines.TimelineNotFoundException(
                    String.format("timeline `%s` doesn't exist", name)
                );
            }
            return new MongoTimeline(this.mongo, cursor.next());
        } finally {
            cursor.close();
        }
    }

    /**
     * Collection.
     * @return Mongo collection
     */
    private DBCollection collection() {
        try {
            return this.mongo.get().getCollection("timelines");
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
