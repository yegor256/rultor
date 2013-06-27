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
import com.jcabi.manifests.Manifests;
import com.rultor.spi.Drain;
import com.rultor.spi.Instance;
import com.rultor.spi.Signal;
import com.rultor.spi.Unit;
import com.rultor.spi.Work;
import java.util.Date;
import lombok.EqualsAndHashCode;

/**
 * Loggable instance.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@EqualsAndHashCode(of = { "origin", "appender", "work" })
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
     * Work we're doing.
     */
    private final transient Work work;

    /**
     * Which unit we're working in.
     */
    private final transient Unit unit;

    /**
     * Public ctor.
     * @param instance Origin
     * @param appr Appender
     * @param wrk Work
     * @param unt Unit
     * @checkstyle ParameterNumber (5 lines)
     */
    protected LoggableInstance(final Instance instance,
        final ConveyerAppender appr, final Work wrk, final Unit unt) {
        this.origin = instance;
        this.appender = appr;
        this.work = wrk;
        this.unit = unt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void pulse() throws Exception {
        final Drain drain = this.unit.drain();
        this.appender.register(this.work.started(), drain);
        try {
            Logger.info(this, "log started on %s", new Date());
            Logger.info(
                this,
                "www.rultor.com %s %s %s",
                Manifests.read("Rultor-Version"),
                Manifests.read("Rultor-Revision"),
                Manifests.read("Rultor-Date")
            );
            Signal.log(Signal.Mnemo.SPEC, this.work.spec().asText());
            this.origin.pulse();
            // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            Signal.log(Signal.Mnemo.FAILURE, ex.getMessage());
            throw ex;
        } finally {
            this.appender.unregister();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.origin.toString();
    }

}
