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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.board.Billboard;
import com.rultor.scm.Head;
import com.rultor.scm.SCM;
import com.rultor.shell.Batch;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Instance;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import javax.json.Json;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directives;

/**
 * Build on every tag.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "scm", "batch", "board" })
@Loggable(Loggable.DEBUG)
public final class OnTag implements Instance {

    /**
     * SCM to monitor.
     */
    private final transient SCM scm;

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
     * @param src Source control
     * @param btch Batch to use
     * @param brd The board where to announce
     */
    public OnTag(
        @NotNull(message = "scm can't be NULL") final SCM src,
        @NotNull(message = "batch can't be NULL") final Batch btch,
        @NotNull(message = "board can't be NULL") final Billboard brd) {
        this.scm = src;
        this.batch = btch;
        this.board = brd;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        for (String tag : this.scm.branches()) {
            this.build(tag);
        }
    }

    /**
     * Build this particular branch.
     * @param tag Branch name to build
     * @throws Exception If fails
     */
    @Step(
        before = "building `${args[0]}`",
        value = "built successfully `${args[0]}`"
    )
    private void build(final String tag) throws Exception {
        this.log(tag);
        new OnCommit(
            new Head(this.scm.checkout(tag)),
            this.batch, this.board
        ).pulse();
    }

    /**
     * Log a tag.
     * @param tag Tag we're building
     * @throws IOException If fails
     */
    private void log(final String tag) throws IOException {
        final StringWriter data = new StringWriter();
        Json.createGenerator(data)
            .writeStartObject()
            .write("name", tag)
            .writeEnd()
            .close();
        final String desc = String.format("tag `%s` has been built", tag);
        new XemblyLine(
            new Directives()
                .xpath("/snapshot").strict(1).addIfAbsent("tags")
                // @checkstyle MultipleStringLiterals (1 line)
                .add("tag").add("label").set("tag").up()
                .add("level").set(Level.INFO.toString()).up()
                .add("data").set(data.toString()).up()
                .add("markdown").set(desc)
        ).log();
    }

}
