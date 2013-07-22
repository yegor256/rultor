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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.board.Billboard;
import com.rultor.scm.Branch;
import com.rultor.scm.Commit;
import com.rultor.shell.Batch;
import com.rultor.spi.Instance;
import com.rultor.spi.Signal;
import com.rultor.stateful.Notepad;
import java.io.IOException;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Build on every new commit.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "branch", "notepad", "batch", "board" })
@Loggable(Loggable.DEBUG)
public final class OnCommit implements Instance {

    /**
     * Branch to monitor.
     */
    private final transient Branch branch;

    /**
     * Notepad where to track all commits.
     */
    private final transient Notepad notepad;

    /**
     * Batch to execute.
     */
    private final transient Batch batch;

    /**
     * Where to notify about success/failure.
     */
    private final transient Billboard board;

    /**
     * Public ctor.
     * @param brn Branch
     * @param ntp Notepad
     * @param btch Batch to use
     * @param brd The board where to announce
     * @checkstyle ParameterNumber (9 lines)
     */
    public OnCommit(@NotNull(message = "branch can't be NULL") final Branch brn,
        @NotNull(message = "notepad can't be NULL") final Notepad ntp,
        @NotNull(message = "batch can't be NULL") final Batch btch,
        @NotNull(message = "board can't be NULL") final Billboard brd) {
        this.branch = brn;
        this.notepad = ntp;
        this.batch = btch;
        this.board = brd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        final Iterator<Commit> commits = this.branch.log().iterator();
        if (commits.hasNext()) {
            final Commit head = commits.next();
            if (this.notepad.contains(head.name())) {
                Signal.log(
                    Signal.Mnemo.SUCCESS,
                    "HEAD was already seen: %s",
                    head
                );
            } else {
                Signal.log(
                    Signal.Mnemo.SUCCESS,
                    "Found new HEAD of SCM: %s",
                    head
                );
                this.build(head);
                this.notepad.add(head.name());
                Signal.log(
                    Signal.Mnemo.SUCCESS,
                    "HEAD %s marked as seen",
                    head
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            // @checkstyle LineLength (1 line)
            "on new commits at %s executes %s and announces through %s, tracks commits at %s",
            this.branch,
            this.notepad,
            this.batch,
            this.board
        );
    }

    /**
     * Build.
     * @param head Head of the branch
     * @throws IOException If some IO problem
     */
    private void build(final Commit head) throws IOException {
        this.board.announce(
            new Build(this.batch).exec(
                new ImmutableMap.Builder<String, Object>()
                    .put("branch", this.branch)
                    .put("head", head)
                    .build()
            )
        );
    }

}
