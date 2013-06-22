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
package com.rultor.aws;

import com.rultor.spi.Pulse;
import com.rultor.spi.Stage;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Protocol}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 */
public final class ProtocolTest {

    /**
     * Protocol can parse text.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesText() throws Exception {
        final String key = "hi";
        final String value = "\u20ac\t\n\n";
        // @checkstyle StringLiteralsConcatenation (4 lines)
        final String text = "10:51 INFO some first line\n"
            + "10:55 INFO " + new Pulse.Signal(key, value) + "\n"
            + "10:57 DEBUG some other line";
        MatcherAssert.assertThat(
            new Protocol(
                new Protocol.Source() {
                    @Override
                    public InputStream stream() throws IOException {
                        return IOUtils.toInputStream(text);
                    }
                }
            ).find(key, ""),
            Matchers.equalTo(value)
        );
    }

    /**
     * Protocol can find stages.
     * @throws Exception If some problem inside
     */
    @Test
    public void findsStages() throws Exception {
        // @checkstyle StringLiteralsConcatenation (4 lines)
        final String text = "11:51 INFO some text \u20ac\n"
            + "11:55 INFO " + new Pulse.Signal(Pulse.Signal.STAGE, "a ") + "\n"
            + "11:57 DEBUG some other text line\n"
            + "11:55 DEBUG " + new Pulse.Signal(Pulse.Signal.STAGE, "b ");
        MatcherAssert.assertThat(
            new Protocol(
                new Protocol.Source() {
                    @Override
                    public InputStream stream() throws IOException {
                        return IOUtils.toInputStream(text);
                    }
                }
            ).stages(),
            Matchers.<Stage>hasItems(
                new Stage.Simple(Stage.Result.SUCCESS, 0, 0, "a"),
                new Stage.Simple(Stage.Result.SUCCESS, 0, 0, "b")
            )
        );
    }

}
