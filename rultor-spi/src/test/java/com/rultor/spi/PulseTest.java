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
package com.rultor.spi;

import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Pulse}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 */
public final class PulseTest {

    /**
     * Pulse can find stages.
     * @throws Exception If some problem inside
     */
    @Test
    public void findsStages() throws Exception {
        // @checkstyle StringLiteralsConcatenation (4 lines)
        final String text = "11:51 INFO Bar some text \u20ac"
            + "\n11:55 INFO Bar " + new Signal(Signal.Mnemo.SUCCESS, "a ")
            + "\n11:57 SEVERE Bar some other text line"
            + "\n11:55 INFO B " + new Signal(Signal.Mnemo.FAILURE, "b ");
        final Drain drain = Mockito.mock(Drain.class);
        Mockito.doReturn(IOUtils.toInputStream(text))
            .when(drain).read(Mockito.anyLong());
        MatcherAssert.assertThat(
            new Pulse(
                1, drain
            ).stages(),
            Matchers.<Stage>hasItems(
                // @checkstyle MagicNumber (2 lines)
                new Stage.Simple(Stage.Result.SUCCESS, 0, 715000, "a"),
                new Stage.Simple(Stage.Result.FAILURE, 0, 715000, "b")
            )
        );
    }

}
