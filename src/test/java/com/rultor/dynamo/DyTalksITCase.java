/**
 * Copyright (c) 2009-2017, rultor.com
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
package com.rultor.dynamo;

import co.stateful.mock.MkSttc;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.manifests.Manifests;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Description;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Integration case for {@link DyTalks}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class DyTalksITCase {

    /**
     * DyTalks can add a talk.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsTalks() throws Exception {
        final Talks talks = new DyTalks(
            this.dynamo(), new MkSttc().counters().get("")
        );
        final String name = "a5fe445";
        talks.create("hey/you", name);
        MatcherAssert.assertThat(
            talks.get(name).read(),
            XhtmlMatchers.hasXPath("/talk[@later]")
        );
    }

    /**
     * DyTalks can list recent talks.
     * @throws Exception If some problem inside
     */
    @Test
    public void listsRecentTalks() throws Exception {
        final Talks talks = new DyTalks(
            this.dynamo(), new MkSttc().counters().get("")
        );
        final String name = "yegor256/rultor#529";
        talks.create("a/b", name);
        final Talk talk = talks.get(name);
        talk.active(false);
        MatcherAssert.assertThat(
            talks.recent(),
            Matchers.hasItem(new DyTalksITCase.TalkMatcher(name))
        );
    }

    /**
     * DyTalks caches talks.
     * @throws Exception If some problem inside
     */
    @Test
    @Ignore
    public void cachesRecentTalks() throws Exception {
        final Talks talks = new DyTalks(
            this.dynamo(), new MkSttc().counters().get("")
        );
        final String first = "krzyk1/rultor#562";
        final String repo = "some/other";
        talks.create(repo, first);
        final Talk talk = talks.get(first);
        talk.active(false);
        MatcherAssert.assertThat(
            talks.recent(),
            Matchers.hasItem(new DyTalksITCase.TalkMatcher(first))
        );
        final String second = "krzyk2/rultor#562#2";
        talks.create(repo, second);
        final Talk talking = talks.get(second);
        talking.active(false);
        MatcherAssert.assertThat(
            talks.recent(),
            Matchers.not(
                Matchers.hasItem(new DyTalksITCase.TalkMatcher(second))
            )
        );
    }

    /**
     * DyTalks can list siblings.
     * @throws Exception If some problem inside
     */
    @Test
    public void listsSiblings() throws Exception {
        final Talks talks = new DyTalks(
            this.dynamo(), new MkSttc().counters().get("")
        );
        final String repo = "repo1";
        talks.create(repo, "yegor256/rultor#9");
        final Date date = new Date();
        TimeUnit.SECONDS.sleep(2L);
        talks.create(repo, "yegor256/rultor#10");
        TimeUnit.SECONDS.sleep(2L);
        MatcherAssert.assertThat(
            talks.siblings(repo, new Date()),
            Matchers.<Talk>iterableWithSize(2)
        );
        MatcherAssert.assertThat(
            talks.siblings(repo, date),
            Matchers.<Talk>iterableWithSize(1)
        );
    }

    /**
     * DyTalks can list recent talks, ignoring private ones.
     * @throws Exception If some problem inside
     */
    @Test
    public void listsRecentTalksExceptPrivates() throws Exception {
        final Talks talks = new DyTalks(
            this.dynamo(), new MkSttc().counters().get("")
        );
        final String name = "yegor256/rultor#990";
        talks.create("a/ff", name);
        final Talk talk = talks.get(name);
        talk.active(false);
        talk.modify(new Directives().xpath("/talk").attr("public", "false"));
        MatcherAssert.assertThat(
            talks.recent(),
            Matchers.not(
                Matchers.hasItem(
                    new CustomMatcher<Talk>("private talk") {
                        @Override
                        public boolean matches(final Object item) {
                            final Talk tlk = Talk.class.cast(item);
                            try {
                                return name.equals(tlk.name());
                            } catch (final IOException ex) {
                                throw new IllegalStateException(ex);
                            }
                        }
                    }
                )
            )
        );
    }

    /**
     * DynamoDB region for tests.
     * @return Region
     */
    private Region dynamo() {
        final String key = Manifests.read("Rultor-DynamoKey");
        Assume.assumeNotNull(key);
        final String port = System.getProperty("dynamo.port");
        Assume.assumeNotNull(port);
        MatcherAssert.assertThat(key.startsWith("AAAA"), Matchers.is(true));
        return new Region.Prefixed(
            new ReRegion(
                new Region.Simple(
                    new Credentials.Direct(
                        new Credentials.Simple(
                            key,
                            Manifests.read("Rultor-DynamoSecret")
                        ),
                        Integer.parseInt(port)
                    )
                )
            ),
            "rt-"
        );
    }

    /**
     * Matcher for Talks.
     */
    private static final class TalkMatcher extends TypeSafeMatcher<Talk> {
        /**
         * Name of the talk.
         */
        private final transient String name;

        /**
         * Constructor.
         * @param nam Name of the talk.
         */
        public TalkMatcher(final String nam) {
            super();
            this.name = nam;
        }

        @Override
        public boolean matchesSafely(final Talk talk) {
            try {
                return talk.name().equals(this.name);
            } catch (final IOException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText(
                String.format("Talk '%s' not found", this.name)
            );
        }
    }
}
