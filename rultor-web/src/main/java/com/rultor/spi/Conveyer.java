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
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Conveyer.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
public interface Conveyer extends Closeable {

    /**
     * Start it.
     */
    void start();

    /**
     * Log it accepts.
     */
    @Immutable
    public interface Log {
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
         * Logger.
         * @return Who is sending it
         */
        @NotNull
        String logger();
        /**
         * Level.
         * @return Level
         */
        @NotNull
        Level level();
        /**
         * Message.
         * @return The message
         */
        @NotNull
        String message();
        /**
         * Simple.
         */
        @Loggable(Loggable.DEBUG)
        @ToString
        @EqualsAndHashCode(of = { "who", "lvl", "msg" })
        @Immutable
        final class Simple implements Line {
            /**
             * Logger.
             */
            private final transient String who;
            /**
             * Level.
             */
            private final transient Level lvl;
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
                this.lvl = level;
                this.msg = message;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public String logger() {
                return this.who;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public Level level() {
                return this.lvl;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public String message() {
                return this.msg;
            }
        }
    }

}
