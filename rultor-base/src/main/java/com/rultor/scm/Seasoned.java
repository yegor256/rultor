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
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Seasoned branch.
 *
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @version $Id$
 */
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
public final class Seasoned implements Branch {

    /**
     * Origin branch.
     */
    private final transient Branch origin;

    /**
     * Holds mimimum age of commit in millis.
     */
    private final transient long commitsAfter;

    /**
     * Public ctor.
     * @param min Minimum age in minutes of commit.
     * @param brn Branch
     */
    public Seasoned(final int min,
        @NotNull(message = "branch can't be NULL") final Branch brn) {
        this.origin = brn;
        this.commitsAfter = TimeUnit.MINUTES.toMillis(min);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("HEAD of %s", this.origin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<Commit> log() throws IOException {
        final Iterator<Commit> iterator = this.origin.log().iterator();
        return new Iterable<Commit>() {
            private final transient Time current = new Time();
            @Override
            public Iterator<Commit> iterator() {
                return Iterators.filter(
                    iterator,
                    new Predicate<Commit>() {
                        @Override
                        public boolean apply(final Commit commit) {
                            return Seasoned.this.checkdelta(commit, current);
                        }
                    }
                );
            }
            @Override
            public String toString() {
                return "Seasoned commits";
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.origin.name();
    }

    /**
     * Checks delta of current time and commit time.
     *
     * @param commit Commit.
     * @param currenttime Present time in millis.
     * @return True if committed on or before commitsAfter.
     */
    private boolean checkdelta(final Commit commit, final Time currenttime) {
        boolean isolder;
        try {
            isolder = commit.time().delta(currenttime) <= this.commitsAfter;
        } catch (IOException ioe) {
            isolder = false;
        }
        return isolder;
    }
}
