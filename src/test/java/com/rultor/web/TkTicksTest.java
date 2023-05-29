/**
 * Copyright (c) 2009-2023 Yegor Bugayenko
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
package com.rultor.web;

import com.jcabi.aspects.Tv;
import com.rultor.spi.Pulse;
import com.rultor.spi.Tick;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.cactoos.bytes.BytesOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.takes.Take;
import org.takes.rq.RqFake;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkTicks}.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.50
 */
public final class TkTicksTest {

    /**
     * TkTicks can render PNG.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersPngStatusImage() throws Exception {
        final Take home = new TkTicks(
            // @checkstyle AnonInnerLengthCheck (50 lines)
            new Pulse() {
                @Override
                public void add(final Tick tick) {
                    throw new UnsupportedOperationException("#add()");
                }
                @Override
                public Iterable<Tick> ticks() {
                    return Arrays.asList(
                        new Tick(1L, 1L, 1),
                        new Tick(2L, 1L, 1)
                    );
                }
                @Override
                public Iterable<Throwable> error() {
                    throw new UnsupportedOperationException("#error()");
                }
                @Override
                public void error(final Iterable<Throwable> errors) {
                    throw new UnsupportedOperationException("#error(..)");
                }
            }
        );
        final BufferedImage image = ImageIO.read(
            new ByteArrayInputStream(
                new BytesOf(
                    new RsPrint(home.act(new RqFake())).body()
                ).asBytes()
            )
        );
        MatcherAssert.assertThat(
            image.getWidth(),
            Matchers.equalTo(Tv.THOUSAND)
        );
    }

    /**
     * TkTicks can render PNG without ticks.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersPngWithoutTicks() throws Exception {
        final Take home = new TkTicks(Pulse.EMPTY);
        MatcherAssert.assertThat(
            new RsPrint(home.act(new RqFake())).asString(),
            Matchers.notNullValue()
        );
    }

}
