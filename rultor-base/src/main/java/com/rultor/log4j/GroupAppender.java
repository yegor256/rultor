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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.log.Logger;
import com.rultor.spi.Drain;
import com.rultor.tools.Exceptions;
import com.rultor.tools.Time;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j log appender used solely in Conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = { "group", "start", "drain" })
@SuppressWarnings("PMD.DoNotUseThreads")
@ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.SECONDS)
final class GroupAppender extends AppenderSkeleton
    implements Runnable, Appender {

    /**
     * Map of levels.
     */
    private static final ImmutableMap<Level, java.util.logging.Level> LEVELS =
        new ImmutableMap.Builder<Level, java.util.logging.Level>()
            .put(Level.ALL, java.util.logging.Level.ALL)
            .put(Level.DEBUG, java.util.logging.Level.FINE)
            .put(Level.ERROR, java.util.logging.Level.SEVERE)
            .put(Level.FATAL, java.util.logging.Level.SEVERE)
            .put(Level.INFO, java.util.logging.Level.INFO)
            .put(Level.OFF, java.util.logging.Level.OFF)
            .put(Level.TRACE, java.util.logging.Level.FINEST)
            .put(Level.WARN, java.util.logging.Level.WARNING)
            .build();

    /**
     * Queue of lines to log.
     */
    private final transient BlockingQueue<String> lines =
        new LinkedBlockingQueue<String>();

    /**
     * The group we're in.
     */
    private final transient ThreadGroup group =
        Thread.currentThread().getThreadGroup();

    /**
     * When the work was scheduled.
     */
    private final transient Time start;

    /**
     * Drain.
     */
    private final transient Drain drain;

    /**
     * Public ctor.
     * @param date When it starts
     * @param drn Drain to log to
     */
    protected GroupAppender(@NotNull(message = "date can't be NULL")
        final Time date, @NotNull(message = "drain can't be NULL")
        final Drain drn) {
        super();
        this.start = date;
        this.drain = drn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public void run() {
        final Collection<String> all = new LinkedList<String>();
        this.lines.drainTo(all);
        try {
            this.drain.append(all);
        // @checkstyle IllegalCatch (1 line)
        } catch (Throwable ex) {
            Logger.error(this, "#append(): %s", Exceptions.message(ex));
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>We swallow exception here in order to enable normal processing
     * of a logging event through LOG4j even when this particular drain
     * fails (this may happen and often).
     */
    @Override
    protected void append(final LoggingEvent event) {
        if (Thread.currentThread().getThreadGroup().equals(this.group)) {
            this.lines.add(
                new Drain.Line.Simple(
                    new Time().delta(this.start),
                    GroupAppender.LEVELS.get(event.getLevel()),
                    this.layout.format(event)
                ).toString()
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.run();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

}
