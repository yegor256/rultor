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
package com.rultor.shell;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.timeline.Product;
import com.rultor.timeline.Tag;
import com.rultor.timeline.Timeline;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Batch that resonates to a {@link Timeline}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "origin", "timeline", "success", "failure" })
@Loggable(Loggable.DEBUG)
public final class Resonant implements Batch {

    /**
     * Original batch.
     */
    private final transient Batch origin;

    /**
     * Timeline to resonate to.
     */
    private final transient Timeline timeline;

    /**
     * Text on success.
     */
    private final transient String success;

    /**
     * Text on failure.
     */
    private final transient String failure;

    /**
     * Public ctor.
     * @param batch Original batch
     * @param tmln Timeline to resonate to
     * @param good Message on success
     * @param bad Message on failure
     * @checkstyle ParameterNumber (8 lines)
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Resonant(
        @NotNull(message = "batch can't be NULL") final Batch batch,
        @NotNull(message = "script can't be NULL") final Timeline tmln,
        @NotNull(message = "good can't be NULL") final String good,
        @NotNull(message = "bad can't be NULL") final String bad) {
        this.origin = batch;
        this.timeline = tmln;
        this.success = good;
        this.failure = bad;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public int exec(final Map<String, Object> args, final OutputStream output)
        throws IOException {
        final int code = this.origin.exec(args, output);
        if (code == 0) {
            this.timeline.submit(
                this.success,
                Arrays.<Tag>asList(new Tag.Simple("success", Level.INFO)),
                new ArrayList<Product>(0)
            );
        } else {
            this.timeline.submit(
                this.failure,
                Arrays.<Tag>asList(new Tag.Simple("failure", Level.SEVERE)),
                new ArrayList<Product>(0)
            );
        }
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "%s resonated to %s",
            this.origin,
            this.timeline
        );
    }

}
