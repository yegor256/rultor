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

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.log.Logger;
import com.rultor.board.Billboard;
import com.rultor.scm.Branch;
import com.rultor.scm.Commit;
import com.rultor.shell.Batch;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.TagLine;
import com.rultor.spi.Instance;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * Build on every new commit.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "branch", "batch", "board" })
@Loggable(Loggable.DEBUG)
public final class OnCommit implements Instance {

    /**
     * Branch to monitor.
     */
    private final transient Branch branch;

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
     * @param btch Batch to use
     * @param brd The board where to announce
     */
    public OnCommit(
        @NotNull(message = "branch can't be NULL") final Branch brn,
        @NotNull(message = "batch can't be NULL") final Batch btch,
        @NotNull(message = "board can't be NULL") final Billboard brd) {
        this.branch = brn;
        this.batch = btch;
        this.board = brd;
    }

    @Override
    @Loggable(
        value = Loggable.DEBUG, limit = Integer.MAX_VALUE,
        ignore = IOException.class
    )
    public void pulse() throws Exception {
        for (final Commit commit : Iterables.limit(this.branch.log(), 1)) {
            this.build(commit);
        }
    }

    /**
     * Build.
     * @param head Head of the branch
     * @return TRUE if success
     * @throws IOException If some IO problem
     */
    @Step(
        before = "building `${args[0].name}`",
        // @checkstyle LineLength (1 line)
        value = "#if($result)succeeded#{else}failed#end to build `${args[0].name}`"
    )
    private boolean build(final Commit head) throws IOException {
        final long start = System.currentTimeMillis();
        final int code = this.batch.exec(
            new ArrayMap<String, String>().with("commit", head.name()),
            new NullOutputStream()
        );
        final long millis = System.currentTimeMillis() - start;
        final boolean success = code == 0;
        new TagLine("on-commit")
            .fine(success)
            .attr("code", Integer.toString(code))
            .attr("duration", Long.toString(millis))
            .attr("branch", this.branch.name())
            .attr("head", head.name())
            .attr("author", head.author())
            .attr("time", head.time().toString())
            .markdown(
                Logger.format(
                    "commit `%s` by %s %s in %[ms]s",
                    StringUtils.substring(head.name(), 0, Tv.SEVEN),
                    head.author(),
                    // @checkstyle AvoidInlineConditionals (1 line)
                    code == 0 ? "succeeded" : "failed",
                    millis
                )
            )
            .log();
        return this.announce(success);
    }

    /**
     * Announce result and return success status.
     * @param success TRUE if success
     * @return The same flag
     * @throws IOException If fails
     */
    @Step("announced #if($args[0])succeess#{else}failure#end")
    private boolean announce(final boolean success) throws IOException {
        this.board.announce(success);
        return success;
    }

}
