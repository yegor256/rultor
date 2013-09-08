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
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Instance;
import com.rultor.tools.Exceptions;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import javax.json.Json;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;

/**
 * Build on every new commit.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
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

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        for (Commit commit : this.branch.log()) {
            this.build(commit);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "on new commits at %s executes %s and announces through %s",
            this.branch, this.batch, this.board
        );
    }

    /**
     * Build.
     * @param head Head of the branch
     * @throws IOException If some IO problem
     */
    @Step(
        before = "building `${args[0]}`",
        value = "built successfully `${args[0]}`"
    )
    private void build(final Commit head) throws IOException {
        this.tag(head);
        final Snapshot snapshot = new Build("on-commit", this.batch).exec(
            new ImmutableMap.Builder<String, Object>()
                .put("branch", this.branch.name())
                .put("head", head)
                .build()
        );
        String xml;
        try {
            xml = snapshot.xml().toString();
        } catch (ImpossibleModificationException ex) {
            xml = String.format(
                "<snapshot><error>%s</error></snapshot>",
                Exceptions.stacktrace(ex)
            );
        }
        this.announce(xml);
    }

    /**
     * Announce result and return success status.
     * @param snapshot Snapshot to announce
     * @throws IOException If fails
     */
    @Step("announced result")
    private void announce(final String snapshot) throws IOException {
        this.board.announce(snapshot);
    }

    /**
     * Log a tag.
     * @param commit Commit we're integrating
     * @throws IOException If fails
     */
    private void tag(final Commit commit) throws IOException {
        final StringWriter data = new StringWriter();
        Json.createGenerator(data)
            .writeStartObject()
            .write("name", commit.name())
            .write("author", commit.author())
            .write("time", commit.time().toString())
            .writeEnd()
            .close();
        final String desc = String.format(
            "commit `%s` by %s on %s",
            commit.name(), commit.author(), commit.time()
        );
        new XemblyLine(
            new Directives()
                .xpath("/snapshot").strict(1).addIfAbsent("tags")
                .add("tag").add("label").set("ci").up()
                .add("level").set(Level.INFO.toString()).up()
                .add("data").set(data.toString()).up()
                .add("markdown").set(desc)
        ).log();
    }

}
