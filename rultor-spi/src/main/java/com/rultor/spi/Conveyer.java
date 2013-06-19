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
import java.io.Closeable;
import java.util.Date;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Mutable and thread-safe conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
public interface Conveyer extends Closeable, Metricable {

    /**
     * Start it.
     */
    void start();

    /**
     * Log it accepts.
     */
    @Immutable
    interface Log extends Closeable {
        /**
         * Consume new log line.
         * @param work Work that produces this line
         * @param line Log line
         */
        void push(@NotNull Work work, @NotNull Line line);
    }

    /**
     * One line in logs.
     */
    @Immutable
    interface Line {
        /**
         * Simple.
         */
        @Immutable
        @Loggable(Loggable.DEBUG)
        @EqualsAndHashCode(of = { "who", "lvl", "msg" })
        final class Simple implements Conveyer.Line {
            /**
             * Logger.
             */
            private final transient String who;
            /**
             * Level.
             */
            private final transient String lvl;
            /**
             * Message.
             */
            private final transient String msg;
            /**
             * Public ctor.
             * @param logger Logger
             * @param level The level
             * @param message The message
             */
            public Simple(final String logger, final Level level,
                final String message) {
                this.who = logger;
                this.lvl = level.toString();
                this.msg = message;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public String toString() {
                return String.format(
                    "%tM:%<tS %s %s %s",
                    new Date(),
                    this.lvl,
                    this.who,
                    this.msg
                );
            }
        }
    }

}
