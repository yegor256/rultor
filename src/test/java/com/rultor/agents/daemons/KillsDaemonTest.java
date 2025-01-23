/*
 * Copyright (c) 2009-2025 Yegor Bugayenko
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
package com.rultor.agents.daemons;

import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.Time;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.xembly.Directives;

/**
 * Tests for ${@link KillsDaemon}.
 *
 * @since 1.0
 */
final class KillsDaemonTest {

    /**
     * KillsDaemon can ignore a daemon.
     * @throws Exception In case of error.
     */
    @Test
    void ignoresFreshDaemon() throws Exception {
        final Talk talk = new Talk.InFile();
        talk.modify(
            new Directives().xpath("/talk").add("daemon")
                .attr("id", "abcd")
                .add("script").set("empty").up()
                .add("title").set("something new").up()
                .add("started")
                .set(new Time().iso())
        );
        final Agent agent = new KillsDaemon();
        agent.execute(talk);
        MatcherAssert.assertThat(
            "KillsDaemon stops daemon older then 1h by default",
            talk.read(),
            XhtmlMatchers.hasXPath("/talk/daemon")
        );
    }

}
