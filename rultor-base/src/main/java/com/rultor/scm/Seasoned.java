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
package com.rultor.scm;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Seasoned branch.
 *
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @version $Id$
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin", "minimum" })
@Loggable(Loggable.DEBUG)
public final class Seasoned implements Branch {

    /**
     * Origin branch.
     */
    private final transient Branch origin;

    /**
     * Holds mimimum age of commit in millis.
     */
    private final transient long minimum;

    /**
     * Public ctor.
     * @param min Minimum age in minutes of commit.
     * @param brn Branch
     */
    public Seasoned(final int min,
        @NotNull(message = "branch can't be NULL") final Branch brn) {
        this.origin = brn;
        this.minimum = TimeUnit.MINUTES.toMillis(min);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Tv.FIVE)
    public Iterable<Commit> log() throws IOException {
        final Time current = new Time();
        return Iterables.filter(
            this.origin.log(),
            new Predicate<Commit>() {
                @Override
                public boolean apply(final Commit commit) {
                    return Seasoned.this.isBefore(commit, current);
                }
            }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.origin.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SCM scm() {
        return this.origin.scm();
    }

    /**
     * Checks delta of current time and commit time.
     *
     * @param commit Commit.
     * @param current Present time in millis.
     * @return True if committed on or before given time.
     */
    private boolean isBefore(final Commit commit, final Time current) {
        try {
            return current.delta(commit.time()) >= this.minimum;
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }
}
