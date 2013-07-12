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
package com.rultor.board;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Loggable;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Announcement.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@ToString
@EqualsAndHashCode(of = { "lvl", "arguments" })
@Loggable(Loggable.DEBUG)
public final class Announcement {

    /**
     * Level of announcement.
     */
    private final transient Level lvl;

    /**
     * Arguments.
     */
    private final transient Map<String, Object> arguments;

    /**
     * Public ctor.
     * @param level Level
     * @param args Arguments
     */
    public Announcement(@NotNull(message = "log level can't be NULL")
        final Level level,
        @NotNull(message = "map of arguments can't be NULL")
        final Map<String, Object> args) {
        this.lvl = level;
        this.arguments = args;
    }

    /**
     * Get level.
     * @return Level
     */
    @NotNull(message = "log Level is never NULL")
    public Level level() {
        return this.lvl;
    }

    /**
     * Get all arguments.
     * @return Arguments
     */
    @NotNull(message = "map of arguments is never NULL")
    public Map<String, Object> args() {
        return this.arguments;
    }

    /**
     * Make a new one, with this extra argument.
     * @param name Argument name
     * @param value The value
     * @return Announcement
     */
    @NotNull(message = "announcement is never NULL")
    public Announcement with(
        @NotNull(message = "name can't be NULL") final String name,
        @NotNull(message = "value can't be NULL") final Object value) {
        return new Announcement(
            this.lvl,
            new ImmutableMap.Builder<String, Object>()
                .putAll(this.arguments)
                .put(name, value)
                .build()
        );
    }

}
