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
import com.rultor.timeline.Product;
import com.rultor.timeline.Tag;
import com.rultor.timeline.Timeline;
import com.rultor.timeline.Timelines;
import com.rultor.tools.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.CustomMatcher;
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
@SuppressWarnings({ "unchecked", "PMD.TooManyMethods" })
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
        final Timelines timelines = this.timelines();
        final URN owner = new URN("urn:test:1");
        final String name = RandomStringUtils.randomAlphabetic(Tv.TEN);
        final Timeline timeline = timelines.create(owner, name);
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

    /**
     * MongoTimelines can manage timelines without conflicts.
     * @throws Exception If some problem inside
     */
    @Test
    public void timelinesDontConflic() throws Exception {
        final Timelines timelines = this.timelines();
        final URN owner = new URN(
            String.format("urn:test:%d", System.currentTimeMillis())
        );
        final Timeline first = timelines.create(
            owner, RandomStringUtils.randomAlphabetic(Tv.TEN)
        );
        first.post("hi", new ArrayList<Tag>(0), new ArrayList<Product>(0));
        MatcherAssert.assertThat(
            timelines.create(owner, RandomStringUtils.randomAlphabetic(Tv.TEN))
                .events(new Time())
                .iterator().hasNext(),
            Matchers.is(false)
        );
    }

    /**
     * MongoTimelines can avoid duplicates.
     * @throws Exception If some problem inside
     */
    @Test(expected = Timelines.TimelineExistsException.class)
    public void avoidsDuplicatedTimelines() throws Exception {
        final Timelines timelines = this.timelines();
        final URN owner = new URN("urn:test:77");
        final String name = RandomStringUtils.randomAlphabetic(Tv.TEN);
        timelines.create(owner, name);
        timelines.create(owner, name);
    }

    /**
     * MongoTimelines can handle tags.
     * @throws Exception If some problem inside
     */
    @Test
    public void savesAndLoadsTags() throws Exception {
        final Timelines timelines = this.timelines();
        final URN owner = new URN("urn:test:980");
        final String name = RandomStringUtils.randomAlphabetic(Tv.TEN);
        final Timeline timeline = timelines.create(owner, name);
        final Tag good = new Tag.Simple("good \u20ac", Level.INFO);
        final Tag bad = new Tag.Simple("bad \u20ac", Level.SEVERE);
        timeline.post(
            "hi there, world! \u20ac",
            Arrays.asList(good, bad),
            new ArrayList<Product>(0)
        );
        MatcherAssert.assertThat(
            timeline.events(new Time()).iterator().next().tags(),
            Matchers.hasItems(
                new CustomMatcher<Tag>("valid good tag") {
                    @Override
                    public boolean matches(final Object obj) {
                        return Tag.class.cast(obj).level().equals(Level.INFO);
                    }
                },
                new CustomMatcher<Tag>("valid bad tag") {
                    @Override
                    public boolean matches(final Object obj) {
                        return Tag.class.cast(obj).level().equals(Level.SEVERE);
                    }
                }
            )
        );
    }

    /**
     * MongoTimelines can handle products.
     * @throws Exception If some problem inside
     */
    @Test
    public void savesAndLoadsProducts() throws Exception {
        final Timelines timelines = this.timelines();
        final URN owner = new URN("urn:test:765");
        final String name = RandomStringUtils.randomAlphabetic(Tv.TEN);
        final Timeline timeline = timelines.create(owner, name);
        final Product first = new Product.Simple("first \u20ac", "hey");
        final Product second = new Product.Simple("second \u20ac", "man");
        timeline.post(
            "hi there, Mr. World! \u20ac",
            new ArrayList<Tag>(0),
            Arrays.asList(first, second)
        );
        MatcherAssert.assertThat(
            timeline.events(new Time()).iterator().next().products(),
            Matchers.hasItems(
                new CustomMatcher<Product>("valid first product") {
                    @Override
                    public boolean matches(final Object obj) {
                        return Product.class.cast(obj).name().startsWith("fir");
                    }
                },
                new CustomMatcher<Product>("valid second product") {
                    @Override
                    public boolean matches(final Object obj) {
                        return Product.class.cast(obj).name().startsWith("sec");
                    }
                }
            )
        );
    }

    /**
     * MongoTimelines can handle aggregated products.
     * @throws Exception If some problem inside
     */
    @Test
    public void savesAndLoadsAggregatedProducts() throws Exception {
        final Timelines timelines = this.timelines();
        final Timeline timeline = timelines.create(
            new URN("urn:test:7699"), RandomStringUtils.randomAlphabetic(Tv.TEN)
        );
        final String name = "my-product";
        final String first = "first value";
        final String second = "second value";
        timeline.post(
            "first calculation of a product",
            new ArrayList<Tag>(0),
            Arrays.<Product>asList(new Product.Simple(name, first))
        );
        timeline.post(
            "second calculation of a product",
            new ArrayList<Tag>(0),
            Arrays.<Product>asList(new Product.Simple(name, second))
        );
        timeline.post(
            "event without a product",
            new ArrayList<Tag>(0),
            new ArrayList<Product>(0)
        );
        MatcherAssert.assertThat(
            timeline.products(),
            Matchers.<Product>iterableWithSize(1)
        );
        MatcherAssert.assertThat(
            timeline.products(),
            Matchers.hasItem(
                new CustomMatcher<Product>("valid result of aggregation") {
                    @Override
                    public boolean matches(final Object obj) {
                        final Product product = Product.class.cast(obj);
                        return product.name().equals(name)
                            && product.markdown().equals(second);
                    }
                }
            )
        );
    }

    /**
     * Get timelines to test against.
     * @return Timelines to test
     * @throws Exception If some problem inside
     */
    private Timelines timelines() throws Exception {
        Assume.assumeNotNull(MongoTimelinesITCase.HOST);
        return new MongoTimelines(
            new Mongo.Simple(
                MongoTimelinesITCase.HOST,
                Integer.parseInt(MongoTimelinesITCase.PORT),
                MongoTimelinesITCase.NAME,
                MongoTimelinesITCase.USER,
                MongoTimelinesITCase.PWD
            )
        );
    }

}
