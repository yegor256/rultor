/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.dynamo;

import co.stateful.mock.MkSttc;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.Env;
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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Integration case for {@link DyTalks}.
 *
 * @since 1.1
 * @checkstyle NonStaticMethodCheck (500 lines)
 */
final class DyTalksITTestCase {

    /**
     * DynamoDB Local port.
     */
    private static final String PORT = System.getProperty("failsafe.ddl.port");

    /**
     * Before the entire test.
     */
    @BeforeEach
    void before() {
        Assumptions.assumeFalse(
            DyTalksITTestCase.PORT == null
            || DyTalksITTestCase.PORT.isEmpty()
        );
    }

    /**
     * DyTalks can add a talk.
     * @throws Exception If some problem inside
     */
    @Test
    void addsTalks() throws Exception {
        final Talks talks = new DyTalks(
            DyTalksITTestCase.dynamo(), new MkSttc().counters().get("")
        );
        final String name = "a5fe445";
        talks.create("hey/you", name);
        MatcherAssert.assertThat(
            "Talk should be with later attribute",
            talks.get(name).read(),
            XhtmlMatchers.hasXPath("/talk[@later]")
        );
    }

    /**
     * DyTalks can list recent talks.
     * @throws Exception If some problem inside
     */
    @Test
    void listsRecentTalks() throws Exception {
        final Talks talks = new DyTalks(
            DyTalksITTestCase.dynamo(), new MkSttc().counters().get("")
        );
        final String name = "yegor256/rultor#529";
        talks.create("a/b", name);
        final Talk talk = talks.get(name);
        talk.active(false);
        MatcherAssert.assertThat(
            "Recent talk should be selected",
            talks.recent(),
            Matchers.hasItem(new DyTalksITTestCase.TalkMatcher(name))
        );
    }

    /**
     * DyTalks caches talks.
     * @throws Exception If some problem inside
     */
    @Test
    @Disabled
    void cachesRecentTalks() throws Exception {
        final Talks talks = new DyTalks(
            DyTalksITTestCase.dynamo(), new MkSttc().counters().get("")
        );
        final String first = "krzyk1/rultor#562";
        final String repo = "some/other";
        talks.create(repo, first);
        final Talk talk = talks.get(first);
        talk.active(false);
        MatcherAssert.assertThat(
            "Recent talk should be received",
            talks.recent(),
            Matchers.hasItem(new DyTalksITTestCase.TalkMatcher(first))
        );
        final String second = "krzyk2/rultor#562#2";
        talks.create(repo, second);
        final Talk talking = talks.get(second);
        talking.active(false);
        MatcherAssert.assertThat(
            "may be it is not true, as test is disabled",
            talks.recent(),
            Matchers.not(
                Matchers.hasItem(new DyTalksITTestCase.TalkMatcher(second))
            )
        );
    }

    /**
     * DyTalks can list siblings.
     * @throws Exception If some problem inside
     */
    @Test
    void listsSiblings() throws Exception {
        final Talks talks = new DyTalks(
            DyTalksITTestCase.dynamo(), new MkSttc().counters().get("")
        );
        final String repo = "repo1";
        talks.create(repo, "yegor256/rultor#9");
        final Date date = new Date();
        TimeUnit.SECONDS.sleep(2L);
        talks.create(repo, "yegor256/rultor#10");
        TimeUnit.SECONDS.sleep(2L);
        MatcherAssert.assertThat(
            "All talks should be returned",
            talks.siblings(repo, new Date()),
            Matchers.iterableWithSize(2)
        );
        MatcherAssert.assertThat(
            "Only one talk should be returned",
            talks.siblings(repo, date),
            Matchers.iterableWithSize(1)
        );
    }

    /**
     * DyTalks can list recent talks, ignoring private ones.
     * @throws Exception If some problem inside
     */
    @Test
    void listsRecentTalksExceptPrivates() throws Exception {
        final Talks talks = new DyTalks(
            DyTalksITTestCase.dynamo(), new MkSttc().counters().get("")
        );
        final String name = "yegor256/rultor#990";
        talks.create("a/ff", name);
        final Talk talk = talks.get(name);
        talk.active(false);
        talk.modify(new Directives().xpath("/talk").attr("public", "false"));
        MatcherAssert.assertThat(
            "Private talks should not be in recent list",
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
    private static Region dynamo() {
        final String key = Env.read("Rultor-DynamoKey");
        Assumptions.assumingThat(key != null, () -> { });
        MatcherAssert.assertThat(
            "Key should be valid",
            key.startsWith("AAAA"),
            Matchers.is(true)
        );
        return new Region.Prefixed(
            new ReRegion(
                new Region.Simple(
                    new Credentials.Direct(
                        new Credentials.Simple(
                            key,
                            Env.read("Rultor-DynamoSecret")
                        ),
                        Integer.parseInt(DyTalksITTestCase.PORT)
                    )
                )
            ),
            "rt-"
        );
    }

    /**
     * Matcher for Talks.
     * @since 1.1
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
        TalkMatcher(final String nam) {
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
