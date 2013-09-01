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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Queue.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Queue {

    /**
     * Push new work into it.
     * @param work The work to do
     */
    void push(@NotNull(message = "work can't be NULL") Work work);

    /**
     * Pull the next available work (waits until it is available).
     * @param limit How many time rules to wait for, maximum
     * @param unit Time unit
     * @return The work available or Work.None if nothing is available now
     * @throws InterruptedException If interrupted while waiting
     */
    @NotNull(message = "work is never NULL")
    Work pull(int limit, @NotNull(message = "time unit can't be NULL")
        TimeUnit unit) throws InterruptedException;

    /**
     * In memory.
     */
    @Loggable(Loggable.DEBUG)
    @ToString
    @EqualsAndHashCode(of = "list")
    final class Memory implements Queue {
        /**
         * Queue of them.
         */
        private final transient BlockingQueue<Work> list =
            new LinkedBlockingQueue<Work>();
        /**
         * {@inheritDoc}
         */
        @Override
        public void push(@NotNull(message = "work can't be NULL")
            final Work work) {
            this.list.add(work);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull
        @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
        public Work pull(final int limit,
            @NotNull(message = "time unit can't be NULL") final TimeUnit unit)
            throws InterruptedException {
            return this.list.poll(limit, unit);
        }
    }

}
