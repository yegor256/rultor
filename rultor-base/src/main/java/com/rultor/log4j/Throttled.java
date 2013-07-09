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
package com.rultor.log4j;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.rultor.spi.Drain;
import com.rultor.spi.Instance;
import com.rultor.spi.Signal;
import com.rultor.spi.Time;
import com.rultor.spi.Work;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

/**
 * Throttled instance.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@EqualsAndHashCode(of = { "work", "level", "origin", "drn" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class Throttled implements Instance, Drain.Source {

    /**
     * The work we're in.
     */
    private final transient Work work;

    /**
     * Log level to show.
     */
    private final transient Level level;

    /**
     * Instance.
     */
    private final transient Instance origin;

    /**
     * Drain to log into.
     */
    private final transient Drain drn;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param lvl Level to show and higher
     * @param instance Original instance
     * @param drain Drain to use
     * @checkstyle ParameterNumber (4 lines)
     */
    public Throttled(@NotNull final Work wrk, @NotNull final String lvl,
        @NotNull final Instance instance, @NotNull final Drain drain) {
        this.work = wrk;
        this.level = Level.toLevel(lvl);
        this.origin = instance;
        this.drn = drain;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void pulse() throws Exception {
        final ConveyerAppender appender = new ConveyerAppender(
            this.work.started(), this.drn
        );
        appender.setThreshold(this.level);
        appender.setLayout(new PatternLayout("%m"));
        org.apache.log4j.Logger.getRootLogger().addAppender(appender);
        try {
            Logger.info(this, "start scheduled on %s", this.work.started());
            Logger.info(this, "actual work started on %s", new Time());
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
            org.apache.log4j.Logger.getRootLogger().removeAppender(appender);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.origin.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drain drain() {
        return this.drn;
    }

}
