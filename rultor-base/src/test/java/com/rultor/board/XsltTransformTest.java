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
package com.rultor.board;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link XsltTransform}.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class XsltTransformTest {
    private static class SavingBillBoard implements Billboard {
        /**
         * Body data received.
         */
        private transient String data = "";

        /**
         * {@inheritDoc}
         */
        @Override
        public void announce(final String body) {
            this.data = body;
        }

        /**
         * Retrieve stored data.
         * @return Data stored.
         */
        private String getData() {
            return this.data;
        }
    }

    /**
     * Simple transformation test.
     * @throws Exception In case of error.
     */
    @Test
    public void simple() throws Exception {
        final XsltTransformTest.SavingBillBoard board =
            new XsltTransformTest.SavingBillBoard();
        new XsltTransform(
            StringUtils.join(
                "<?xml version=\"1.0\"?>",
                // @checkstyle LineLength (1 line)
                "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">",
                "<xsl:template match=\"/body\">",
                "<test><xsl:value-of select=\"value\"/></test>",
                "</xsl:template>",
                "</xsl:stylesheet>"
            ),
            board
        ).announce("<body><value>Text</value></body>");
        MatcherAssert
            .assertThat(board.getData(), Matchers.equalTo("<test>Text</test>"));
    }

    /**
     * Wrong XSL transformation.
     * @throws Exception In case of error.
     */
    @Test(expected = IllegalArgumentException.class)
    public void wrongArgument() throws Exception {
        new XsltTransform(
            "",
            new XsltTransformTest.SavingBillBoard()
        ).announce("<value>Text</value>");
    }
}
