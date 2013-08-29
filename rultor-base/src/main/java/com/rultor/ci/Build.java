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
import com.rultor.shell.Batch;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.XemblyLine;
import com.rultor.tools.Exceptions;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.xembly.Directives;
import org.xembly.XemblySyntaxException;

/**
 * Build.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "batch", "product" })
@Loggable(Loggable.DEBUG)
public final class Build {

    /**
     * Batch to execute.
     */
    private final transient Batch batch;

    /**
     * Name of the product we're building.
     */
    private final transient String product;

    /**
     * Public ctor.
     * @param pdt Product name
     * @param btch Batch to use
     */
    public Build(
        @NotNull(message = "product name can't be NULL") final String pdt,
        @NotNull(message = "batch can't be NULL") final Batch btch) {
        this.product = pdt;
        this.batch = btch;
    }

    /**
     * Build and return a snapshot/XML.
     * @param args Arguments to pass to the batch
     * @return XML of snapshot
     * @throws IOException If some IO problem
     */
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public Snapshot exec(@NotNull(message = "args can't be NULL")
        final Map<String, Object> args) throws IOException {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final int code = this.batch.exec(args, stdout);
        Snapshot snapshot;
        try {
            snapshot = new Snapshot(
                new SequenceInputStream(
                    new ByteArrayInputStream(stdout.toByteArray()),
                    IOUtils.toInputStream(this.tag(code), Charsets.UTF_8)
                )
            );
        } catch (XemblySyntaxException ex) {
            snapshot = new Snapshot(
                new Directives().add("error").set(Exceptions.stacktrace(ex))
            );
        }
        return snapshot;
    }

    /**
     * Make a xembly tag when done.
     * @param code The code
     * @return Marker
     */
    private String tag(final int code) {
        final Level level;
        if (code == 0) {
            level = Level.FINE;
        } else {
            level = Level.SEVERE;
        }
        final XemblyLine line = new XemblyLine(
            new Directives()
                .xpath("/snapshot").strict(1).addIfAbsent("tags")
                .xpath(String.format("tag[label='%s']", this.product))
                .remove().xpath("/snapshot/tags").strict(1)
                .add("tag").add("label").set(this.product).up()
                .add("level").set(level.toString())
        );
        line.log();
        return line.toString();
    }

}
