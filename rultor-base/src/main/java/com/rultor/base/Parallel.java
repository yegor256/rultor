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
package com.rultor.base;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Pulseable;
import com.rultor.spi.State;
import com.rultor.spi.Work;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Enables certain amount of parallel pulses.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin", "maximum" })
@Loggable(Loggable.DEBUG)
public final class Parallel implements Pulseable {

    /**
     * Are we busy right now trying to calculate something?
     */
    private static final String BUSY = String.format(
        "%s.busy", Parallel.class.getCanonicalName()
    );

    /**
     * How many active threads are running now (key in State).
     */
    private static final String ACTIVE = String.format(
        "%s.active", Parallel.class.getCanonicalName()
    );

    /**
     * Origin.
     */
    private final transient Pulseable origin;

    /**
     * Maximum.
     */
    private final transient int maximum;

    /**
     * Public ctor.
     * @param max Maximum
     * @param pls Original pulseable
     */
    public Parallel(final int max, final Pulseable pls) {
        this.origin = pls;
        this.maximum = max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pulse(@NotNull final Work work, @NotNull final State state)
        throws Exception {
        final int active = this.safety(
            state,
            new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    final int active = Parallel.active(state) + 1;
                    if (active <= Parallel.this.maximum) {
                        assert state.checkAndSet(
                            Parallel.ACTIVE, Integer.toString(active)
                        );
                    }
                    return active;
                }
            }
        );
        if (active <= Parallel.this.maximum) {
            try {
                this.origin.pulse(work, state);
            } finally {
                this.safety(
                    state,
                    new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            assert state.checkAndSet(
                                Parallel.ACTIVE,
                                Integer.toString(Parallel.active(state) - 1)
                            );
                            return null;
                        }
                    }
                );
            }
        }
    }

    /**
     * Call this callable safely.
     * @param state State
     * @param callable Callable to call
     * @return Result
     */
    private <T> T safety(final State state, final Callable<T> callable)
        throws Exception {
        while (!state.checkAndSet(Parallel.BUSY, "on")) {
            TimeUnit.SECONDS.sleep(1);
        }
        T result = callable.call();
        assert state.checkAndSet(Parallel.BUSY, "off");
        return result;
    }

    /**
     * Fetch the number of active running threads.
     * @param state State
     * @return Active threads count
     */
    private static int active(final State state) {
        int active;
        if (state.has(Parallel.ACTIVE)) {
            active = Integer.parseInt(state.get(Parallel.ACTIVE));
        } else {
            active = 0;
        }
        return active;
    }

}
