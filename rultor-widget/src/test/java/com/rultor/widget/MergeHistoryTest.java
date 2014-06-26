/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.widget;

import com.jcabi.immutable.ArrayMap;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.urn.URN;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pulse;
import com.rultor.spi.Pulses;
import com.rultor.spi.Stand;
import com.rultor.spi.Tag;
import com.rultor.spi.Tags;
import com.rultor.spi.Widget;
import java.util.Arrays;
import java.util.logging.Level;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for {@link MergeHistory}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class MergeHistoryTest {

    /**
     * MergeHistory can find recent merges.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("unchecked")
    public void findsRecentMerges() throws Exception {
        final Widget widget = new MergeHistory();
        final Pulse first = Mockito.mock(Pulse.class);
        Mockito.doReturn(
            new Coordinates.Simple(new URN("urn:test:44"), "rule-x")
        ).when(first).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple(
                        "on-pull-request", Level.SEVERE,
                        new ArrayMap<String, String>()
                            .with("code", "127")
                            .with("duration", "9870")
                            .with("name", "554"),
                        ""
                    )
                )
            )
        ).when(first).tags();
        final Pulse second = Mockito.mock(Pulse.class);
        Mockito.doReturn(
            new Coordinates.Simple(new URN("urn:test:54"), "rule-y")
        ).when(second).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple(
                        "on-pull-request", Level.INFO,
                        new ArrayMap<String, String>()
                            .with("code", "0")
                            .with("duration", "99892")
                            .with("name", "554"),
                        ""
                    )
                )
            )
        ).when(second).tags();
        final Pulses pulses = new Pulses.Row(Arrays.asList(first, second));
        final Stand stand = Mockito.mock(Stand.class);
        Mockito.doReturn(pulses).when(stand).pulses();
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("widget").append(widget.render(stand))
            ).xml(),
            XhtmlMatchers.hasXPaths(
                "/widget[not(title)]",
                "/widget[width='6']",
                "/widget/merges[count(merge)=2]",
                "/widget/merges/merge/coordinates[owner='urn:test:54']",
                "/widget/merges/merge/coordinates[owner='urn:test:44']",
                "/widget/merges/merge/coordinates[rule='rule-x']",
                "/widget/merges/merge[name='554']",
                "/widget/merges/merge[code=127 and duration=9870]",
                "/widget/merges/merge[code=0 and duration=99892]"
            )
        );
    }

    /**
     * MergeHistory can gracefully handle broken tags.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("unchecked")
    public void gracefullyHandlesEmptyTags() throws Exception {
        final Widget widget = new MergeHistory();
        final Pulse first = Mockito.mock(Pulse.class);
        final Coordinates coords = new Coordinates.Simple(
            new URN("urn:test:3"), "rule-a"
        );
        Mockito.doReturn(coords).when(first).coordinates();
        Mockito.doReturn(
            new Tags.Simple(
                Arrays.<Tag>asList(
                    new Tag.Simple("on-pull-request", Level.SEVERE)
                )
            )
        ).when(first).tags();
        final Pulses pulses = new Pulses.Row(Arrays.asList(first));
        final Stand stand = Mockito.mock(Stand.class);
        Mockito.doReturn(pulses).when(stand).pulses();
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("widget").append(widget.render(stand))
            ).xml(),
            XhtmlMatchers.hasXPath("/widget/merges[count(merge)=1]")
        );
    }

    /**
     * BuildHistory can report empty widget.
     * @throws Exception If fails
     */
    @Test
    @SuppressWarnings("unchecked")
    public void reportsEmptyWidgetWhenNoTagsFound() throws Exception {
        final Widget widget = new MergeHistory();
        final Pulses pulses = new Pulses.Row();
        final Stand stand = Mockito.mock(Stand.class);
        Mockito.doReturn(pulses).when(stand).pulses();
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("widget").append(widget.render(stand))
            ).xml(),
            XhtmlMatchers.hasXPath("/widget/merges[count(merge) = 0]")
        );
    }

    /**
     * MergeHistory can render XML+XSL with Phandom.
     * @throws Exception If fails
     */
    @Test
    public void rendersXmlInPhandom() throws Exception {
        MatcherAssert.assertThat(
            WidgetMocker.xhtml(
                this.getClass().getResource("merge-history.xml")
            ),
            XhtmlMatchers.hasXPath("//xhtml:table")
        );
    }

}
