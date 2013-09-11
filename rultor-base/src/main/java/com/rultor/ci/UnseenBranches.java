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
package com.rultor.ci;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.scm.Branch;
import com.rultor.scm.SCM;
import com.rultor.snapshot.Step;
import com.rultor.stateful.Notepad;
import java.io.IOException;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Returns only one tag, if it wasn't seen before.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin", "notepad" })
@Loggable(Loggable.DEBUG)
public final class UnseenBranches implements SCM {

    /**
     * Branch to monitor.
     */
    private final transient SCM origin;

    /**
     * Notepad where to track all commits.
     */
    private final transient Notepad notepad;

    /**
     * Public ctor.
     * @param scm SCM original
     * @param ntp Notepad
     */
    public UnseenBranches(
        @NotNull(message = "SCM can't be NULL") final SCM scm,
        @NotNull(message = "notepad can't be NULL") final Notepad ntp) {
        this.origin = scm;
        this.notepad = ntp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Branch checkout(final String name) throws IOException {
        return this.origin.checkout(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<String> branches() throws IOException {
        final Iterator<String> iterator = this.origin.branches().iterator();
        return new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                return Iterators.filter(
                    iterator,
                    new Predicate<String>() {
                        @Override
                        public boolean apply(final String branch) {
                            return !UnseenBranches.this.seen(branch);
                        }
                    }
                );
            }
        };
    }

    /**
     * This branch was seen already?
     * @param branch Branch name
     * @return TRUE if seen
     */
    @Step("tag `${args[0]}` #if(!$result)NOT#end seen before")
    private boolean seen(final String branch) {
        final boolean seen = this.notepad.contains(branch);
        if (!seen) {
            this.notepad.add(branch);
        }
        return seen;
    }

}
