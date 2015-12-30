/**
 * Copyright (c) 2009-2015, rultor.com
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
package com.rultor.agents;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talks;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Tests for {@link com.rultor.agents.IndexesRequests}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class IndexesRequestsTest {
    /**
     * IndexesRequests should store index when it doesn't exist.
     * @throws Exception In case of error.
     */
    @Test
    public void storesIndexIfNone() throws Exception {
        final String name = "talk";
        final Talks talks = new Talks.InDir();
        talks.create("", name);
        talks.get(name).modify(
            new Directives()
                .xpath("/talk").push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#1").up().up()
                .add("request").attr("id", "a12345")
                .add("args").up()
                .add("type").set("deploy").up()
        );
        new IndexesRequests().execute(talks);
        MatcherAssert.assertThat(
            talks.get(name).read(),
            XhtmlMatchers.hasXPaths("/talk/request[@index='1']")
        );
    }

    /**
     * IndexesRequests should retrieve index from log.
     * @throws Exception In case of error.
     */
    @Test
    public void retrievesIndexFromLog() throws Exception {
        final String name = "talk";
        final Talks talks = new Talks.InDir();
        talks.create("", name);
        talks.get(name).modify(
            new Directives().xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#2").up().up()
                .add("archive")
                .add("log").attr("id", "1").attr("title", "title1")
                .attr("index", "1").up()
                .add("log").attr("id", "2").attr("title", "title2")
                .attr("index", "2").up().up()
                .add("request").attr("id", "a12345")
                .add("args").up()
                .add("type").set("deploy").up()
        );
        new IndexesRequests().execute(talks);
        MatcherAssert.assertThat(
            talks.get(name).read(),
            XhtmlMatchers.hasXPaths("/talk/request[@index='3']")
        );
    }

    /**
     * IndexesRequests should retrieve index from sibling
     * (the test is skipped, more information in #733).
     * @throws Exception In case of error.
     */
    @Test
    public void retrievesIndexFromSibling() throws Exception {
        final String first = "first";
        final Talks talks = new Talks.InDir();
        talks.create("", first);
        talks.get(first).modify(
            new Directives().xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#3").up().up()
                .add("archive")
                .add("log").attr("id", "3").attr("title", "title3")
                .attr("index", "1").up()
        );
        final String second = "second";
        talks.create("", second);
        talks.get(second).modify(
            new Directives().xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#4").up().up()
                .add("archive")
                .add("log").attr("id", "4").attr("title", "title4")
                .attr("index", "2").up()
        );
        final String third = "third";
        talks.create("", third);
        talks.get(third).modify(
            new Directives()
                .xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#5").up().up()
                .add("request").attr("id", "a67890")
                .add("args").up()
                .add("type").set("merge").up()
        );
        new IndexesRequests().execute(talks);
        MatcherAssert.assertThat(
            talks.get(third).read(),
            XhtmlMatchers.hasXPaths("/talk/request[@index='5']")
        );
    }

    /**
     * IndexesRequests should not store index when request tag doesn't exist.
     * @throws Exception In case of error.
     */
    @Test
    public void notStoreIndexWithoutRequest() throws Exception {
        final String name = "talk";
        final Talks talks = new Talks.InDir();
        talks.create("", name);
        talks.get(name).modify(
            new Directives()
                .xpath("/talk")
                .push().xpath("wire").remove().pop()
                .add("wire").add("href").set("#1").up()
        );
        new IndexesRequests().execute(talks);
        MatcherAssert.assertThat(
            talks.get(name).read(),
            Matchers.not(
                XhtmlMatchers.hasXPaths("/talk/request")
            )
        );
    }
}
