/**
 * Copyright (c) 2009-2019, rultor.com
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

import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XMLDocument;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Tests for {@link Profile}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.28
 */
public final class ProfileTest {

    /**
     * Profile.Fixed can accept correct XML.
     * @throws Exception In case of error.
     */
    @Test
    public void acceptsValidXML() throws Exception {
        MatcherAssert.assertThat(
            new Profile.Fixed(
                new XMLDocument(
                    StringUtils.join(
                        "<p><entry key='deploy'>",
                        "<entry key='script'>this is a plain text</entry>",
                        "<entry key='&gt;'><item>hello</item></entry>",
                        "<entry key='abc'><entry key='a'>b</entry></entry>",
                        "</entry></p>"
                    )
                )
            ).read(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='deploy']",
                "/p/entry[@key='deploy']/entry[@key='script']",
                "//entry[@key='script' and .='this is a plain text']"
            )
        );
    }

    /**
     * Profile.Fixed can reject incorrect XML.
     * @throws Exception In case of error.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsMixedEntriesAndItems() throws Exception {
        new Profile.Fixed(
            new XMLDocument(
                StringUtils.join(
                    "<p><entry key='x'><entry key='test'>a</entry>",
                    "<item>b</item></entry></p>"
                )
            )
        );
    }

    /**
     * Profile.Fixed can reject incorrect XML.
     * @throws Exception In case of error.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsEntryWithoutKey() throws Exception {
        new Profile.Fixed(new XMLDocument("<p><entry>test me</entry></p>"));
    }

    /**
     * Profile.Fixed can reject unknown attributes.
     * @throws Exception In case of error.
     */
    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownAttributes() throws Exception {
        new Profile.Fixed(new XMLDocument("<p><entry f='x'>x</entry></p>"));
    }

}
