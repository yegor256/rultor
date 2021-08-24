/**
 * Copyright (c) 2009-2021 Yegor Bugayenko
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
package com.rultor.agents.ecs;

import com.amazonaws.services.ecs.model.Container;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for ${@link com.rultor.agents.ecs.StartsECS}.
 * @author Yuriy Alevohin (alevohin@mail.ru)
 * @version $Id$
 * @since 1.0
 */
public final class StartsECSTest {

    /**
     * StartsECS can start On-Demand Container.
     * @throws Exception In case of error.
     */
    @Test
    public void startsOnDemandContainer() throws Exception {
        final Container container = Mockito.mock(
            Container.class
        );
        Mockito.doReturn("1").when(container).getContainerArn();
        final Amazon amazon = Mockito.mock(Amazon.class);
        Mockito.doReturn(container).when(amazon).runOnDemand();
        final Agent agent = new StartsECS(amazon);
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk")
                .add("daemon").attr("id", "abcd")
                .add("title").set("something").up()
                .add("script").set("test")
        );
        agent.execute(talk);
        MatcherAssert.assertThat(
            talk.read(),
            XhtmlMatchers.hasXPaths(
                "/talk/ec2[@id='1']"
            )
        );
    }
}
