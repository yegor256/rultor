/**
 * Copyright (c) 2009-2018, rultor.com
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
package com.rultor.profiles;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Profile;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for ${@link YamlXML}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class YamlXMLTest {

    /**
     * YamlXML can parse.
     * @throws Exception In case of error.
     */
    @Test
    public void parsesYamlConfig() throws Exception {
        MatcherAssert.assertThat(
            new YamlXML("a: test\nb: 'hello'\nc:\n  - one\nd:\n  f: e").get(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='a' and .='test']",
                "/p/entry[@key='b' and .='hello']",
                "/p/entry[@key='c']/item[.='one']",
                "/p/entry[@key='d']/entry[@key='f' and .='e']"
            )
        );
    }

    /**
     * YamlXML can parse a broken text.
     * @throws Exception In case of error.
     */
    @Test
    public void parsesYamlConfigWhenBroken() throws Exception {
        MatcherAssert.assertThat(
            new YamlXML("a: alpha\nb:\nc:\n  - beta").get(),
            XhtmlMatchers.hasXPaths(
                "/p/entry[@key='a' and .='alpha']",
                "/p/entry[@key='b' and .='']",
                "/p/entry[@key='c']/item[.='beta']"
            )
        );
    }

    /**
     * YamlXML can parse a broken text and throw.
     * @throws Exception In case of error.
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void parsesBrokenConfigsAndThrows() throws Exception {
        final String[] yamls = {
            "thre\n\t\\/\u0000",
            "first: \"привет \\/\t\r\"",
        };
        for (final String yaml : yamls) {
            try {
                new YamlXML(yaml).get();
                Assert.fail(String.format("exception expected for %s", yaml));
            } catch (final Profile.ConfigException ex) {
                continue;
            }
        }
    }

}
