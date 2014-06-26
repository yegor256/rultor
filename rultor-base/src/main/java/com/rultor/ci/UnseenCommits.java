/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.ci;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.scm.Branch;
import com.rultor.scm.Commit;
import com.rultor.scm.SCM;
import com.rultor.snapshot.Step;
import com.rultor.stateful.Notepad;
import java.io.IOException;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Returns only one commit, if it wasn't seen before.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin", "notepad" })
@Loggable(Loggable.DEBUG)
public final class UnseenCommits implements Branch {

    /**
     * Branch to monitor.
     */
    private final transient Branch origin;

    /**
     * Notepad where to track all commits.
     */
    private final transient Notepad notepad;

    /**
     * Public ctor.
     * @param brn Branch
     * @param ntp Notepad
     */
    public UnseenCommits(
        @NotNull(message = "branch can't be NULL") final Branch brn,
        @NotNull(message = "notepad can't be NULL") final Notepad ntp) {
        this.origin = brn;
        this.notepad = ntp;
    }

    @Override
    public String name() {
        return this.origin.name();
    }

    @Override
    @Loggable(
        value = Loggable.DEBUG, limit = Tv.FIVE,
        ignore = IOException.class
    )
    public Iterable<Commit> log() throws IOException {
        final Iterator<Commit> iterator = this.origin.log().iterator();
        return new Iterable<Commit>() {
            @Override
            public Iterator<Commit> iterator() {
                return Iterators.filter(
                    iterator,
                    new Predicate<Commit>() {
                        @Override
                        public boolean apply(final Commit commit) {
                            return !UnseenCommits.this.seen(commit);
                        }
                    }
                );
            }
        };
    }

    @Override
    public SCM scm() {
        return this.origin.scm();
    }

    /**
     * This HEAD commit was seen already?
     * @param head HEAD commit
     * @return TRUE if seen
     */
    @Step("commit `${args[0].name}` #if(!$result)NOT#end seen before")
    private boolean seen(final Commit head) {
        final String name;
        try {
            name = head.name();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        final boolean seen = this.notepad.contains(name);
        if (!seen) {
            this.notepad.add(name);
        }
        return seen;
    }

}
