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
package com.rultor.web;

import com.jcabi.urn.URN;
import com.rultor.client.RtUser;
import com.rultor.spi.Rule;
import com.rultor.spi.Spec;
import com.rultor.spi.User;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Integration case for {@link RulesRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.5
 */
public final class RulesRsITCase {

    /**
     * Home page of Tomcat.
     */
    private static final String HOME = System.getProperty("tomcat.home");

    /**
     * RulesRs can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void rendersPage() throws Exception {
        final User user = new RtUser(
            new URI(RulesRsITCase.HOME), new URN("urn:test:222"), ""
        );
        final String name = "sample-rule";
        if (!user.rules().contains(name)) {
            user.rules().create(name);
        }
        final Rule rule = user.rules().get(name);
        final String[][] pairs = {
            {"java.lang.Double ( -55.0 )", "java.lang.Double(-55.0)"},
            {"\"some text  \u20ac\" ", "\"some text  \\u20AC\""},
        };
        final Spec simple = new Spec.Simple();
        for (final String[] pair : pairs) {
            rule.update(new Spec.Simple(pair[0]), simple);
            MatcherAssert.assertThat(
                rule.spec().asText(), Matchers.equalTo(pair[1])
            );
        }
        final String[] specs = {
            "com.rultor.base.Empty()",
            // @checkstyle LineLength (1 line)
            "com.rultor.base.Restrictive(${work}, ['*'], com.rultor.base.Empty())",
        };
        for (final String spec : specs) {
            rule.update(new Spec.Simple(spec), simple);
        }
        user.rules().remove(name);
    }

}
