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
import com.jcabi.log.Logger;
import com.rultor.snapshot.Step;
import com.rultor.spi.Instance;
import com.rultor.spi.Work;
import com.rultor.stateful.Lineup;
import com.rultor.stateful.Notepad;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * Enables certain amount of parallel pulses.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "work", "active", "origin", "lineup", "maximum" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class Parallel implements Instance {

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Origin.
     */
    private final transient Instance origin;

    /**
     * List of active threads.
     */
    private final transient Notepad active;

    /**
     * Lineup.
     */
    private final transient Lineup lineup;

    /**
     * Maximum.
     */
    private final transient int maximum;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param max Maximum
     * @param lnp Lineup
     * @param atv List of active threads
     * @param instance Original instance
     * @checkstyle ParameterNumber (10 lines)
     */
    public Parallel(
        @NotNull(message = "work can't be NULL") final Work wrk,
        final int max,
        @NotNull(message = "lineup can't be NULL") final Lineup lnp,
        @NotNull(message = "notepad can't be NULL") final Notepad atv,
        @NotNull(message = "instance can't be NULL") final Instance instance) {
        Validate.isTrue(max >= 0, "Maximum can't be negative, %d given", max);
        this.work = wrk;
        this.origin = instance;
        this.lineup = lnp;
        this.maximum = max;
        this.active = atv;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        if (this.maximum > 0) {
            this.pass();
        } else {
            Logger.info(this, "Zero threads allowed, no need to even try");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%s in %d thread(s) synchronized by %s and persisted by %s",
            this.origin,
            this.maximum,
            this.lineup,
            this.active
        );
    }

    /**
     * Pass through.
     * @throws Exception If fails
     */
    private void pass() throws Exception {
        final String key = this.work.scheduled().toString();
        this.lineup.exec(
            new Runnable() {
                @Override
                public void run() {
                    Parallel.this.active.add(key);
                }
                @Override
                public String toString() {
                    return String.format("add %s", Parallel.this.work);
                }
            }
        );
        try {
            if (this.allowed()) {
                this.origin.pulse();
            }
        } finally {
            this.lineup.exec(
                new Runnable() {
                    @Override
                    public void run() {
                        Parallel.this.active.remove(key);
                    }
                    @Override
                    @SuppressWarnings("PMD.ReturnFromFinallyBlock")
                    public String toString() {
                        return String.format("remove %s", Parallel.this.work);
                    }
                }
            );
        }
    }

    /**
     * Execution is allowed?
     * @return TRUE if allowed
     * @checkstyle LineLength (2 lines)
     */
    @Step("parallel #if(!$result)NOT#end allowed with ${this.active.size()} thread(s)")
    private boolean allowed() {
        final boolean allowed = this.active.size() <= this.maximum;
        if (allowed) {
            Logger.info(
                this,
                "%d thread(s) are running now, execution allowed: %[list]s",
                this.active.size(),
                this.active
            );
        } else {
            Logger.info(
                this,
                "%d thread(s) running already (too many): %[list]s",
                this.active.size(),
                this.active
            );
        }
        return allowed;
    }

}
