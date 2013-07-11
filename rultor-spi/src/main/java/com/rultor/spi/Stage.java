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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Stage of a pulse.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Stage {

    /**
     * Result of a stage.
     */
    enum Result {
        /**
         * Still running.
         */
        RUNNING,
        /**
         * Waiting.
         */
        WAITING,
        /**
         * Success.
         */
        SUCCESS,
        /**
         * Failure.
         */
        FAILURE;
    };

    /**
     * Result of the stage.
     * @return Result
     */
    @NotNull(message = "result is never NULL")
    Result result();

    /**
     * When started or is planning to start.
     * @return Milliseconds from the beginning of the pulse
     */
    long start();

    /**
     * When stopped or is planning to stop.
     * @return Milliseconds from the beginning of the pulse
     */
    long stop();

    /**
     * Output to show to the user (one line).
     * @return Output
     */
    @NotNull(message = "output is never NULL")
    String output();

    /**
     * Simple implementation.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "rslt", "begin", "end", "text" })
    @Loggable(Loggable.DEBUG)
    final class Simple implements Stage {
        /**
         * Result.
         */
        private final transient Result rslt;
        /**
         * Start moment, in milliseconds from start.
         */
        private final transient long begin;
        /**
         * Stop moment, in milliseconds from start.
         */
        private final transient long end;
        /**
         * Result.
         */
        private final transient String text;
        /**
         * Public ctor.
         * @param result Result
         * @param start Start moment, in milliseconds from start
         * @param stop Stop moment, in milliseconds from start
         * @param output Text output
         * @checkstyle ParameterNumber (5 lines)
         */
        public Simple(@NotNull(message = "result can't be NULL")
            final Stage.Result result, final long start,
            final long stop, @NotNull(message = "output can't be NULL")
            final String output) {
            this.rslt = result;
            this.begin = start;
            this.end = stop;
            this.text = output;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Result result() {
            return this.rslt;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public long start() {
            return this.begin;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public long stop() {
            return this.end;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String output() {
            return this.text;
        }
    }

}
