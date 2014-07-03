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
import com.rultor.spi.Spec;
import com.rultor.spi.Stand;
import com.rultor.spi.User;
import java.net.URI;
import org.junit.Test;

/**
 * Integration case for {@link RulesRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.5
 */
public final class StandRsITCase {

    /**
     * Home page of Tomcat.
     */
    private static final String HOME = System.getProperty("tomcat.home");

    /**
     * AccountRs can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersPage() throws Exception {
        final User user = new RtUser(
            new URI(StandRsITCase.HOME), new URN("urn:test:222"), ""
        );
        final String name = "sample-unit";
        if (!user.stands().contains(name)) {
            user.stands().create(name);
        }
        final Stand stand = user.stands().get(name);
        stand.update(
            new Spec.Simple("com.rultor.acl.FullAccess()"),
            new Spec.Simple("com.rultor.base.Empty()")
        );
    }

}
