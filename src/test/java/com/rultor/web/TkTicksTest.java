/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.web;

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
 * @since 1.50
 */
final class TkTicksTest {

    /**
     * TkTicks can render PNG.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersPngStatusImage() throws Exception {
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
            "TkTicks should generate png status image",
            image.getWidth(),
            Matchers.equalTo(1_000)
        );
    }

    /**
     * TkTicks can render PNG without ticks.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersPngWithoutTicks() throws Exception {
        final Take home = new TkTicks(Pulse.EMPTY);
        MatcherAssert.assertThat(
            "TkTicks should generate some image without Ticks",
            new RsPrint(home.act(new RqFake())).asString(),
            Matchers.notNullValue()
        );
    }

}
