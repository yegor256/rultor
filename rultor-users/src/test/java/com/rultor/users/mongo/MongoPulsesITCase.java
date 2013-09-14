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

import com.jcabi.aspects.Tv;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.rultor.spi.Pulse;
import java.util.Arrays;
import java.util.logging.Level;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link MongoPulses}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings({ "unchecked", "PMD.TooManyMethods" })
public final class MongoPulsesITCase {

    /**
     * Mongo host.
     */
    private static final String HOST =
        System.getProperty("failsafe.mongo.host");

    /**
     * Mongo port.
     */
    private static final String PORT =
        System.getProperty("failsafe.mongo.port");

    /**
     * Mongo database name.
     */
    private static final String NAME =
        System.getProperty("failsafe.mongo.name");

    /**
     * Mongo user.
     */
    private static final String USER =
        System.getProperty("failsafe.mongo.user");

    /**
     * Mongo password.
     */
    private static final String PWD =
        System.getProperty("failsafe.mongo.password");

    /**
     * MongoPulses can search by tags.
     * @throws Exception If some problem inside
     */
    @Test
    public void searchesByTagInMongoDb() throws Exception {
        final Mongo mongo = this.mongo();
        final DBCollection col = mongo.get().getCollection(MongoStand.TABLE);
        col.remove(new BasicDBObject());
        final String first = "first-tag";
        final String data = "{\"item\": 555}";
        col.insert(
            new BasicDBObject().append(
                MongoStand.ATTR_TAGS,
                Arrays.asList(
                    new MongoTag(first, Level.INFO, data, "").asObject()
                )
            )
        );
        final String second = "second-tag";
        col.insert(
            new BasicDBObject().append(
                MongoStand.ATTR_TAGS,
                Arrays.asList(
                    new MongoTag(second, Level.INFO, data, "").asObject()
                )
            )
        );
        col.insert(
            new BasicDBObject().append(
                MongoStand.ATTR_TAGS,
                Arrays.asList(
                    new MongoTag(first, Level.INFO, data, "").asObject(),
                    new MongoTag(second, Level.INFO, data, "").asObject()
                )
            )
        );
        col.insert(
            new BasicDBObject().append(
                MongoStand.ATTR_TAGS,
                new MongoTag("other-tag", Level.INFO, data, "").asObject()
            )
        );
        MatcherAssert.assertThat(
            new MongoPulses(
                mongo, new Predicate.Any(), new Predicate.Any()
            ),
            Matchers.<Pulse>iterableWithSize(Tv.FOUR)
        );
        MatcherAssert.assertThat(
            new MongoPulses(
                mongo, new Predicate.Any(),
                new Predicate.WithTag(second)
            ),
            Matchers.<Pulse>iterableWithSize(2)
        );
        MatcherAssert.assertThat(
            new MongoPulses(
                mongo, new Predicate.Any(),
                new Predicate.And(
                    new Predicate.WithTag(first),
                    new Predicate.WithTag(second)
                )
            ),
            Matchers.<Pulse>iterableWithSize(1)
        );
        MatcherAssert.assertThat(
            new MongoPulses(
                mongo, new Predicate.Any(), new Predicate.WithTag("nothing")
            ),
            Matchers.<Pulse>iterableWithSize(0)
        );
    }

    /**
     * Get Mongo client to test against.
     * @return Client
     * @throws Exception If some problem inside
     */
    private Mongo mongo() throws Exception {
        Assume.assumeNotNull(MongoPulsesITCase.HOST);
        return new Mongo.Simple(
            MongoPulsesITCase.HOST,
            Integer.parseInt(MongoPulsesITCase.PORT),
            MongoPulsesITCase.NAME,
            MongoPulsesITCase.USER,
            MongoPulsesITCase.PWD
        );
    }

}
