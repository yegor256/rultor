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
package com.rultor.repo;

import com.jcabi.urn.URN;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link AntlrGrammar}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class AntlrGrammarTest {

    /**
     * AntlrGrammar can make a spec.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesSpecFromText() throws Exception {
        final Grammar grammar = new AntlrGrammar();
        final String[] texts = new String[] {
            "java.lang.String(\"te   - st\")",
            "java.lang.Integer(123)",
            "java.lang.Long(-44L)",
            "foo.SomeClass(1, FALSE, TRUE, 8L, \"test\")",
            "java.lang.Double(-44.66)",
            "com.some.type$name($work)",
            "com.first(com.second(com.third(), com.forth()))",
            "\"\"\"\nsome\nunformatted\ttext\t\u20ac\u0433\n\"\"\"",
            "java.lang.String(\n  \"\"\"\n  hello\n  \"\"\"\n)",
        };
        final URN urn = new URN("urn:facebook:1");
        for (String text : texts) {
            MatcherAssert.assertThat(
                grammar.parse(urn, text).asText(),
                Matchers.equalTo(text)
            );
        }
    }

}
