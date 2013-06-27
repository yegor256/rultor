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

import com.google.common.collect.ImmutableMap;
import com.rultor.spi.Drain;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;
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
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings("PMD.DoNotUseThreads")
final class ConveyerAppender extends AppenderSkeleton implements Appender {

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
     * This thread is currently logging, all other logs are forbidden.
     */
    private final transient AtomicBoolean busy = new AtomicBoolean();

    /**
     * Drain starts.
     */
    private final transient ConcurrentMap<ThreadGroup, Long> groups =
        new ConcurrentHashMap<ThreadGroup, Long>(0);

    /**
     * Drains.
     */
    private final transient ConcurrentMap<Long, Drain> drains =
        new ConcurrentHashMap<Long, Drain>(0);

    /**
     * Register this thread group for the given work.
     * @param date When it starts
     * @param drain Drain to log to
     */
    public void register(@NotNull final long date, @NotNull final Drain drain) {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        Validate.validState(
            !this.groups.containsKey(group),
            "group %s is already registered to %s",
            group, this.groups.get(group)
        );
        this.groups.put(group, date);
        Validate.validState(
            !this.drains.containsKey(date),
            "work %d is already drained to %s",
            date, this.drains.get(date)
        );
        this.drains.put(date, drain);
    }

    /**
     * Unregister the current thread group.
     */
    public void unregister() {
        final ThreadGroup group = Thread.currentThread().getThreadGroup();
        Validate.validState(
            this.groups.containsKey(group),
            "group %s is not registered",
            group
        );
        final Long date = this.groups.remove(group);
        Validate.validState(
            this.drains.containsKey(date),
            "work %d is not registered",
            date
        );
        this.drains.remove(date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void append(final LoggingEvent event) {
        if (this.busy.compareAndSet(false, true)) {
            final ThreadGroup group = Thread.currentThread().getThreadGroup();
            final Long date = this.groups.get(group);
            if (date != null) {
                this.drains.get(date).write(
                    date,
                    Arrays.asList(
                        new Drain.Line.Simple(
                            System.currentTimeMillis() - date,
                            ConveyerAppender.LEVELS.get(event.getLevel()),
                            this.layout.format(event)
                        ).toString()
                    )
                );
            }
            this.busy.set(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        this.groups.clear();
        this.drains.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresLayout() {
        return true;
    }

}
