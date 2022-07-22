/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
package com.rultor.agents;

import com.jcabi.log.Logger;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Aggent that tracks time and complains if too slow.
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.59
 */
public final class TimedAgent implements Agent {

    /**
     * Limit in seconds.
     */
    private static final long LIMIT = TimeUnit.SECONDS.toMillis(10L);

    /**
     * Agent.
     */
    private final transient Agent origin;

    /**
     * Ctor.
     * @param agent Original agent
     */
    public TimedAgent(final Agent agent) {
        this.origin = agent;
    }

    @Override
    public void execute(final Talk talk) throws IOException {
        final long start = System.currentTimeMillis();
        this.origin.execute(talk);
        final long msec = System.currentTimeMillis() - start;
        if (msec > TimedAgent.LIMIT) {
            Logger.error(
                this, "%s#execute() took %[ms]s, it's too long!",
                this.origin.getClass().getCanonicalName(),
                msec
            );
        }
    }
}
