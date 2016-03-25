/**
 * Copyright (c) 2009-2016, rultor.com
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
package com.rultor.agents.ec2;

import com.amazonaws.services.ec2.model.Instance;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for ${@link com.rultor.agents.ec2.StartsEC2}.
 * @author Yuriy Alevohin (alevohin@mail.ru)
 * @version $Id$
 * @since 1.0
 */
public final class StartsEC2Test {

    /**
     * StartsEC2 can start On-Demand Instance.
     * @throws Exception In case of error.
     */
    @Test
    public void startsOnDemandInstance() throws Exception {
        final Instance instance = Mockito.mock(Instance.class);
        Mockito.doReturn("1").when(instance).getInstanceId();
        final Amazon amazon = Mockito.mock(Amazon.class);
        Mockito.doReturn(instance).when(amazon).runOnDemand();
        final Agent agent = new StartsEC2(amazon);
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
