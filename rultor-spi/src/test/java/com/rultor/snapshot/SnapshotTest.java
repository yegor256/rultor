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
package com.rultor.snapshot;

import com.rexsl.test.XhtmlMatchers;
import com.rultor.spi.Tag;
import com.rultor.spi.Tags;
import java.util.logging.Level;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link Snapshot}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class SnapshotTest {

    /**
     * Snapshot can fetch snapshot from stream.
     * @throws Exception If some problem inside
     */
    @Test
    public void fetchesSnapshotFromStream() throws Exception {
        MatcherAssert.assertThat(
            new Snapshot(
                IOUtils.toInputStream(
                    new StringBuilder()
                        .append("hey dude!\n")
                        .append(
                            new XemblyLine(
                                new Directives()
                                    // @checkstyle MultipleStringLiterals (1 line)
                                    .xpath("/snapshot")
                                    .strict(1)
                                    .add("test")
                                    .strict(1)
                                    .set("how are you, dude?!")
                            ).toString()
                        )
                        .append("\nHow are you?\n")
                        .toString(),
                    CharEncoding.UTF_8
                )
            ).xml().toString(),
            XhtmlMatchers.hasXPath(
                "/snapshot/test[.='how are you, dude?!']"
            )
        );
    }

    /**
     * Snapshot can fetch tags.
     * @throws Exception If some problem inside
     */
    @Test
    public void fetchesTagsFromXml() throws Exception {
        final String label = "my-tag";
        final Tag tag = new Tags.Simple(
            new Snapshot(
                IOUtils.toInputStream(
                    new XemblyLine(
                        new TagLine(label)
                            .markdown("\u0433 **dude**")
                            .attr("alpha", null)
                            .attr("beta-attr", "hey, \u20ac!")
                            .fine(true)
                            .directives()
                    ).toString(),
                    CharEncoding.UTF_8
                )
            ).tags()
        ).get(label);
        MatcherAssert.assertThat(tag.label(), Matchers.equalTo(label));
        MatcherAssert.assertThat(tag.level(), Matchers.equalTo(Level.FINE));
        MatcherAssert.assertThat(
            tag.markdown(),
            Matchers.containsString(" **dude**")
        );
        MatcherAssert.assertThat(
            tag.attributes(),
            Matchers.hasEntry(
                Matchers.startsWith("beta-"),
                Matchers.startsWith("hey, ")
            )
        );
    }

    /**
     * Snapshot can gracefully handle empty tag.
     * @throws Exception If some problem inside
     */
    @Test
    public void gracefullyHandlesEmptyOrBrokenTag() throws Exception {
        final String label = "test-tag";
        final Tag tag = new Tags.Simple(
            new Snapshot(
                IOUtils.toInputStream(
                    new XemblyLine(
                        new TagLine(label).directives()
                    ).toString(),
                    CharEncoding.UTF_8
                )
            ).tags()
        ).get(label);
        MatcherAssert.assertThat(tag.label(), Matchers.equalTo(label));
        MatcherAssert.assertThat(tag.level(), Matchers.equalTo(Level.INFO));
        MatcherAssert.assertThat(tag.markdown(), Matchers.equalTo(""));
        MatcherAssert.assertThat(
            tag.attributes().keySet(),
            Matchers.empty()
        );
    }

    /**
     * Ignores all tags but last.
     * @throws Exception If problem inside
     */
    @Test
    public void xmlIgnoresAllTagsButLast() throws Exception {
        MatcherAssert.assertThat(
            new Snapshot(
                new Directives()
                    .xpath("/snapshot")
                    .addIf("tags")
                        // @checkstyle MultipleStringLiterals (5 line)
                    .add("tag").add("label").set("unique").up()
                    .add("markdown").set("this is unique tag").up().up()
                    .add("tag").add("label").set("duplicate").up()
                    .add("markdown").set("this is the first tag").up().up()
                    .add("tag").add("label").set("duplicate").up()
                    .add("markdown").set("this is the second tag")
            ).xml(),
            Matchers.allOf(
                XhtmlMatchers.hasXPath(
                    "/snapshot/tags/tag/markdown[contains(.,'unique')]"
                ),
                Matchers.not(
                    XhtmlMatchers.hasXPath(
                        "/snapshot/tags/tag/markdown[contains(.,'first')]"
                    )
                ),
                XhtmlMatchers.hasXPath(
                    "/snapshot/tags/tag/markdown[contains(.,'second')]"
                )
            )
        );
    }

}
