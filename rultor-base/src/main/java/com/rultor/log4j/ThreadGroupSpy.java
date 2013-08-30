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
import com.rultor.spi.Drain;
import com.rultor.spi.Instance;
import com.rultor.spi.Work;
import com.rultor.tools.Exceptions;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Instance wrapper that intercepts all log4j events initiated by any
 * thread in the instance thread group.
 *
 * <p>The wrapper intercepts all log4j logging events coming from any thread
 * that belongs to the thread group of the instance. Use it as a top level
 * wrapper of an instance, for example:
 *
 * <pre> com.rultor.log4j.ThreadGroupSpy(
 *   ${0:?}, "INFO", "%p: %c %m",
 *   instance,
 *   drain
 * )</pre>
 *
 * <p>Log4j logging events lower than the specified threshold are filtered
 * out. All others are rendered using the provided log4j pattern through
 * {@link PatternLayout}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@EqualsAndHashCode(of = { "work", "level", "pattern", "origin", "drn" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class ThreadGroupSpy implements Instance, Drain.Source {

    /**
     * The work we're in.
     */
    private final transient Work work;

    /**
     * Log level to show.
     */
    private final transient Level level;

    /**
     * Pattern.
     */
    private final transient String pattern;

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
     * @param pttn Pattern
     * @param instance Original instance
     * @param drain Drain to use
     * @checkstyle ParameterNumber (8 lines)
     */
    public ThreadGroupSpy(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "log level can't be NULL") final String lvl,
        @NotNull(message = "pattern can't be NULL") final String pttn,
        @NotNull(message = "instance can't be NULL") final Instance instance,
        @NotNull(message = "drain can't be NULL") final Drain drain) {
        this.work = wrk;
        this.level = Level.toLevel(lvl);
        this.pattern = pttn;
        this.origin = instance;
        this.drn = drain;
    }

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param instance Original instance
     * @param drain Drain to use
     * @checkstyle ParameterNumber (4 lines)
     */
    public ThreadGroupSpy(final Work wrk, final Instance instance,
        final Drain drain) {
        this(wrk, Level.INFO.toString(), "%m", instance, drain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        final GroupAppender appender = new GroupAppender(
            this.work.scheduled(), this.drn
        );
        appender.setThreshold(this.level);
        appender.setLayout(new PatternLayout(this.pattern));
        final Logger root = Logger.getRootLogger();
        if (!root.isInfoEnabled()) {
            throw new IllegalStateException(
                // @checkstyle LineLength (1 line)
                "INFO logging level is not enabled in log4j, check log4j.properties file in classpath"
            );
        }
        root.addAppender(appender);
        try {
            this.origin.pulse();
        } finally {
            this.close(appender);
            root.removeAppender(appender);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "%s drained `%s` to %s",
            this.origin,
            this.level,
            this.drn
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drain drain() {
        return this.drn;
    }

    /**
     * Close the appender.
     * @param appender The appender to close
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void close(final Appender appender) {
        try {
            appender.close();
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            com.jcabi.log.Logger.error(
                this, "#close(): %s", Exceptions.message(ex)
            );
        }
    }

}
