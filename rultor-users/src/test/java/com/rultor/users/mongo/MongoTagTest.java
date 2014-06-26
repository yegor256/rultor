/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.jcabi.immutable.ArrayMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rultor.spi.Tag;
import java.util.logging.Level;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link MongoTag}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MongoTagTest {

    /**
     * MongoTag can parse Mongo DBObject.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesMongoDbObject() throws Exception {
        final String label = "test-label";
        final String attr = "alpha";
        final Tag tag = new MongoTag(
            new BasicDBObject()
                .append(MongoTag.ATTR_LABEL, label)
                .append(MongoTag.ATTR_LEVEL, Level.INFO.toString())
                .append(
                    MongoTag.ATTR_ATTRIBUTES,
                    new ArrayMap<String, String>().with(attr, "foo---")
                )
                .append(MongoTag.ATTR_MARKDOWN, "")
        );
        MatcherAssert.assertThat(tag.label(), Matchers.equalTo(label));
        MatcherAssert.assertThat(
            tag.attributes(),
            Matchers.hasKey(attr)
        );
    }

    /**
     * MongoTag can build Mongo DBObject.
     * @throws Exception If some problem inside
     */
    @Test
    public void buildsMongoDbObject() throws Exception {
        final String label = "test";
        final DBObject object = new MongoTag(label, Level.FINE).asObject();
        MatcherAssert.assertThat(
            object.get(MongoTag.ATTR_LABEL).toString(),
            Matchers.equalTo(label)
        );
        MatcherAssert.assertThat(
            object.get(MongoTag.ATTR_LEVEL).toString(),
            Matchers.equalTo(Level.FINE.toString())
        );
    }

}
