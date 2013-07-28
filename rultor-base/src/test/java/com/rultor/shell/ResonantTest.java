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
package com.rultor.shell;

import com.google.common.collect.ImmutableMap;
import com.rultor.timeline.Product;
import com.rultor.timeline.Timeline;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import org.apache.commons.codec.CharEncoding;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link Resonant}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class ResonantTest {

    /**
     * Resonant can resonate.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("unchecked")
    public void resonatesToTimeline() throws Exception {
        final Timeline timeline = Mockito.mock(Timeline.class);
        final ImmutableMap<String, Object> args =
            new ImmutableMap.Builder<String, Object>().build();
        final Batch origin = Mockito.mock(Batch.class);
        final Product product = new Product.Simple("test", "hi, **world**!");
        Mockito.doAnswer(
            new Answer<Integer>() {
                @Override
                public Integer answer(final InvocationOnMock inv)
                    throws IOException {
                    final PrintWriter writer = new PrintWriter(
                        OutputStream.class.cast(inv.getArguments()[1]), true
                    );
                    writer.println(Resonant.encode(product));
                    writer.close();
                    return 0;
                }
            }
        ).when(origin).exec(Mockito.eq(args), Mockito.any(OutputStream.class));
        final String success = "successful execution!";
        final Batch batch = new Resonant(origin, timeline, success, "fail");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        batch.exec(args, baos);
        MatcherAssert.assertThat(
            baos.toString(CharEncoding.UTF_8),
            Matchers.containsString("RULTOR")
        );
        Mockito.verify(timeline).submit(
            Mockito.eq(success),
            Mockito.any(Collection.class),
            Mockito.<Collection<Product>>argThat(
                Matcher.class.cast(Matchers.hasItem(product))
            )
        );
    }

}
