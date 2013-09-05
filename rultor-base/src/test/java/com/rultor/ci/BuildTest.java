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
package com.rultor.ci;

import com.google.common.collect.ImmutableMap;
import com.rexsl.test.XhtmlMatchers;
import com.rultor.shell.Batch;
import com.rultor.snapshot.XemblyLine;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Map;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xembly.Directives;

/**
 * Test case for {@link Build}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class BuildTest {

    /**
     * Build can build and return snapshot.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("unchecked")
    public void buildsAndReturnsSnapshot() throws Exception {
        final Batch batch = Mockito.mock(Batch.class);
        Mockito.doAnswer(
            // @checkstyle AnonInnerLength (50 lines)
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock inv)
                    throws Exception {
                    final PrintWriter stdout = new PrintWriter(
                        new OutputStreamWriter(
                            OutputStream.class.cast(inv.getArguments()[1]),
                            CharEncoding.UTF_8
                        ), true
                    );
                    stdout.println(
                        new XemblyLine(
                            new Directives()
                                .xpath("/snapshot").add("test")
                                .set("\u0433\u0444")
                        )
                    );
                    stdout.close();
                    return null;
                }
            }
        )
            .when(batch)
            .exec(
                Mockito.any(Map.class), Mockito.any(OutputStream.class)
            );
        final Build build = new Build("hey", batch);
        MatcherAssert.assertThat(
            build.exec(
                new ImmutableMap.Builder<String, Object>().build()
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/snapshot[test='\u0433\u0444']",
                "/snapshot/tags/tag[label='hey' and level='FINE']"
            )
        );
    }

    /**
     * Build can handle broken xembly gracefully.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("unchecked")
    public void gracefullyHandlesBrokenXembly() throws Exception {
        final Batch batch = Mockito.mock(Batch.class);
        Mockito.doAnswer(
            new Answer<Void>() {
                @Override
                public Void answer(final InvocationOnMock inv)
                    throws Exception {
                    final PrintWriter stdout = new PrintWriter(
                        new OutputStreamWriter(
                            OutputStream.class.cast(inv.getArguments()[1]),
                            CharEncoding.UTF_8
                        ), true
                    );
                    stdout.print(XemblyLine.MARK);
                    stdout.println(" 'broken content'");
                    stdout.close();
                    return null;
                }
            }
        )
            .when(batch)
            .exec(Mockito.any(Map.class), Mockito.any(OutputStream.class));
        final Build build = new Build("hey-5", batch);
        MatcherAssert.assertThat(
            build.exec(
                new ImmutableMap.Builder<String, Object>().build()
            ).xml(),
            XhtmlMatchers.hasXPath("/snapshot/error")
        );
    }

}
