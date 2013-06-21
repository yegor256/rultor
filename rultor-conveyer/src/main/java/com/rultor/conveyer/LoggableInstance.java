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
package com.rultor.conveyer;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.spi.Instance;
import com.rultor.spi.Pulse;
import com.rultor.spi.Repo;
import com.rultor.spi.Work;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Loggable instance.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode(of = { "origin", "appender" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
final class LoggableInstance implements Instance {

    /**
     * Instance.
     */
    private final transient Instance origin;

    /**
     * Log appender.
     */
    private final transient ConveyerAppender appender;

    /**
     * Public ctor.
     * @param instance Origin
     * @param appr Appender
     */
    protected LoggableInstance(final Instance instance,
        final ConveyerAppender appr) {
        this.origin = instance;
        this.appender = appr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pulse(@NotNull final Work work) throws Exception {
        final Thread thread = Thread.currentThread();
        this.appender.register(thread, work);
        try {
            Logger.info(
                this,
                new Pulse.Signal(
                    Pulse.Signal.SPEC, work.spec().asText()
                ).toString()
            );
            this.origin.pulse(work);
        } catch (Repo.InstantiationException ex) {
            Logger.error(this, "%[exception]s", ex);
        } finally {
            this.appender.unregister(thread);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String face() {
        return this.origin.face();
    }

}
