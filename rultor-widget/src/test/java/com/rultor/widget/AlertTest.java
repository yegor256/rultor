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

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Stand;
import com.rultor.spi.Widget;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Tests for {@link Alert}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class AlertTest {

    /**
     * Alert can show message.
     * @throws Exception If fails
     */
    @Test
    public void showsMessageInWidget() throws Exception {
        final Widget widget = new Alert("test message");
        final Stand stand = Mockito.mock(Stand.class);
        MatcherAssert.assertThat(
            new Xembler(
                new Directives().add("widget").append(widget.render(stand))
            ).xml(),
            XhtmlMatchers.hasXPath("/widget[error='test message']")
        );
    }

    /**
     * Alert can render XML+XSL with Phandom.
     * @throws Exception If fails
     */
    @Test
    public void rendersXmlInPhandom() throws Exception {
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                WidgetMocker.xhtml(
                    this.getClass().getResource("alert.xml")
                )
            ),
            XhtmlMatchers.hasXPath("//xhtml:div[@class='text-danger']")
        );
    }

}
