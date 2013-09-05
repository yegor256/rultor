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
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link SnapshotInStream}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class SnapshotTest {

    /**
     * SnapshotInStream can fetch snapshot from stream.
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
     * SnapshotInStream can properly handle special characters.
     * @throws Exception If some problem inside
     */
    @Test
    @org.junit.Ignore
    public void gracefullyHandlesSpecialCharacters() throws Exception {
        MatcherAssert.assertThat(
            new Snapshot(
                IOUtils.toInputStream(
                    new StringBuilder().append(
                        new XemblyLine(
                            new Directives().add("foo").set(
                                "<&>'\"\u20ac\u001b!"
                            )
                        ).toString()
                    ).toString(),
                    CharEncoding.UTF_8
                )
            ).xml().toString(),
            XhtmlMatchers.hasXPath(
                "/snapshot/foo[.='&lt;&amp;&gt;&apos;&quot;\u20ac\u001b!']"
            )
        );
    }

}
