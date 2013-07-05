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
import com.jcabi.aspects.Tv;
import com.rultor.board.Announcement;
import com.rultor.shell.Batch;
import com.rultor.spi.Signal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

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
    protected Build(@NotNull final Batch btch) {
        this.batch = btch;
    }

    /**
     * Build and return a result.
     * @param args Arguments to pass to the batch
     * @return Announcement of result
     * @throws IOException If some IO problem
     */
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public Announcement exec(@NotNull final Map<String, Object> args)
        throws IOException {
        Signal.log(Signal.Mnemo.START, "Started to build..");
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final int code = this.batch.exec(args, stdout);
        final Announcement announcement;
        if (code == 0) {
            announcement = new Announcement(
                Level.INFO,
                new ImmutableMap.Builder<String, Object>()
                    // @checkstyle MultipleStringLiterals (2 lines)
                    .put("title", "built successfully")
                    .put("stdout", Build.compressed(stdout))
                    .build()
            );
            Signal.log(Signal.Mnemo.SUCCESS, "Announced success");
        } else {
            announcement = new Announcement(
                Level.INFO,
                new ImmutableMap.Builder<String, Object>()
                    .put("title", "failed to build")
                    .put("stdout", Build.compressed(stdout))
                    .build()
            );
            Signal.log(Signal.Mnemo.SUCCESS, "Announced failure");
        }
        return announcement;
    }

    /**
     * Compressed variant of the output.
     * @param stdout The stream
     * @return Text of it
     * @throws IOException If some IO problem
     */
    private static String compressed(final ByteArrayOutputStream stdout)
        throws IOException {
        List<String> lines = Arrays.asList(
            StringUtils.splitPreserveAllTokens(
                stdout.toString(CharEncoding.UTF_8),
                CharUtils.LF
            )
        );
        if (lines.size() > Tv.FIFTY) {
            lines = lines.subList(lines.size() - Tv.FIFTY, lines.size() - 1);
        }
        return StringUtils.join(lines, "\n");
    }

}
