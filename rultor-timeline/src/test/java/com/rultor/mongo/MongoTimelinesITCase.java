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

import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.spi.Time;
import com.rultor.timeline.Product;
import com.rultor.timeline.Tag;
import com.rultor.timeline.Timeline;
import com.rultor.timeline.Timelines;
import java.util.ArrayList;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link MongoTimelines}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class MongoTimelinesITCase {

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
     * MongoTimelines can accept and return works.
     * @throws Exception If some problem inside
     */
    @Test
    public void managesDataInMongoDb() throws Exception {
        Assume.assumeNotNull(MongoTimelinesITCase.HOST);
        final Timelines timelines = new MongoTimelines(
            new Mongo.Simple(
                MongoTimelinesITCase.HOST,
                Integer.parseInt(MongoTimelinesITCase.PORT),
                MongoTimelinesITCase.NAME,
                MongoTimelinesITCase.USER,
                MongoTimelinesITCase.PWD
            )
        );
        final URN owner = new URN("urn:test:1");
        final String name = RandomStringUtils.randomAlphabetic(Tv.TEN);
        timelines.create(owner, name);
        final Timeline timeline = timelines.get(name);
        MatcherAssert.assertThat(timeline.name(), Matchers.equalTo(name));
        MatcherAssert.assertThat(
            timeline.permissions().owner(), Matchers.equalTo(owner)
        );
        MatcherAssert.assertThat(
            timeline.permissions().key().length(), Matchers.equalTo(Tv.TWENTY)
        );
        final String text = "hello, world! \u20ac";
        timeline.post(text, new ArrayList<Tag>(0), new ArrayList<Product>(0));
        MatcherAssert.assertThat(
            timeline.events(new Time()).iterator().next().text(),
            Matchers.equalTo(text)
        );
    }

}
