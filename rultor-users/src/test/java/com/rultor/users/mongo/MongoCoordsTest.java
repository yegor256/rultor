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

import com.jcabi.urn.URN;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rultor.spi.Coordinates;
import com.rultor.tools.Time;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MongoCoords}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MongoCoordsTest {

    /**
     * MongoCoords can parse Mongo DBObject.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesMongoDbObject() throws Exception {
        final String rule = "test-rule-name";
        final Time scheduled = new Time();
        final URN owner = new URN("urn:test:89809");
        final Coordinates coords = new MongoCoords(
            new BasicDBObject()
                .append(MongoCoords.ATTR_OWNER, owner)
                .append(MongoCoords.ATTR_RULE, rule)
                .append(MongoCoords.ATTR_SCHEDULED, scheduled)
        );
        MatcherAssert.assertThat(coords.rule(), Matchers.equalTo(rule));
        MatcherAssert.assertThat(coords.owner(), Matchers.equalTo(owner));
        MatcherAssert.assertThat(
            coords.scheduled(), Matchers.hasToString(scheduled.toString())
        );
    }

    /**
     * MongoCoords can build Mongo DBObject.
     * @throws Exception If some problem inside
     */
    @Test
    public void buildsMongoDbObject() throws Exception {
        final String rule = "test-rule-a";
        final Time scheduled = new Time();
        final URN owner = new URN("urn:test:896666");
        final DBObject object = new MongoCoords(
            new Coordinates.Simple(owner, rule, scheduled)
        ).asObject();
        MatcherAssert.assertThat(
            object.get(MongoCoords.ATTR_OWNER),
            Matchers.hasToString(owner.toString())
        );
        MatcherAssert.assertThat(
            object.get(MongoCoords.ATTR_RULE),
            Matchers.hasToString(rule)
        );
        MatcherAssert.assertThat(
            object.get(MongoCoords.ATTR_SCHEDULED),
            Matchers.hasToString(scheduled.toString())
        );
    }

}
