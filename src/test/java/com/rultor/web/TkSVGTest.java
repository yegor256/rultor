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
package com.rultor.web;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Pulse;
import java.util.Arrays;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.takes.Take;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkSVG}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.50
 */
public final class TkSVGTest {

    /**
     * TkSVG can render SVG.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersSvg() throws Exception {
        final Take home = new TkSVG(
            Arrays.asList(
                new Pulse.Tick(1L, 1L, 1),
                new Pulse.Tick(2L, 1L, 1)
            )
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(new RsPrint(home.act()).printBody()),
            XhtmlMatchers.hasXPaths(
                "/svg:svg",
                "//svg:svg[count(svg:rect) >= 2]"
            )
        );
    }

    /**
     * TkSVG can render SVG without ticks.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersSvgWithouTicks() throws Exception {
        final Take home = new TkSVG(
            Collections.<Pulse.Tick>emptyList()
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(new RsPrint(home.act()).printBody()),
            XhtmlMatchers.hasXPath("//svg:tspan[contains(.,'outage')]")
        );
    }

}
