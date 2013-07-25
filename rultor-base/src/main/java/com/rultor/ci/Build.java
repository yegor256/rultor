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
import com.rultor.board.Announcement;
import com.rultor.shell.ASCIIOutputStream;
import com.rultor.shell.Batch;
import com.rultor.spi.Signal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;

/**
 * Build.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "batch")
@Loggable(Loggable.DEBUG)
final class Build {

    /**
     * Batch to execute.
     */
    private final transient Batch batch;

    /**
     * Public ctor.
     * @param btch Batch to use
     */
    protected Build(@NotNull(message = "batch can't be NULL")
        final Batch btch) {
        this.batch = btch;
    }

    /**
     * Build and return a result.
     * @param args Arguments to pass to the batch
     * @return Announcement of result
     * @throws IOException If some IO problem
     */
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public Announcement exec(@NotNull(message = "args can't be NULL")
        final Map<String, Object> args)
        throws IOException {
        Signal.log(Signal.Mnemo.START, "Started to build");
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final long start = System.currentTimeMillis();
        final int code = this.batch.exec(
            args, new ASCIIOutputStream(stdout)
        );
        final Announcement announcement;
        final ImmutableMap.Builder<String, Object> builder =
            new ImmutableMap.Builder<String, Object>()
                .put("stdout", stdout.toString(CharEncoding.UTF_8))
                .put(
                    "elapsed",
                    String.format(
                        "%[ms]s",
                        System.currentTimeMillis() - start
                    )
                )
                .putAll(args);
        if (code == 0) {
            announcement = new Announcement(
                Level.INFO,
                // @checkstyle MultipleStringLiterals (2 lines)
                builder.put("title", "built successfully").build()
            );
        } else {
            announcement = new Announcement(
                Level.SEVERE,
                builder.put("title", "failed to build").build()
            );
        }
        return announcement;
    }

}
