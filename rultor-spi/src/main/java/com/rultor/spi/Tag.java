/**
 * Copyright (c) 2009-2014, rultor.com
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
import com.jcabi.immutable.ArrayMap;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Tag of a {@link Pulse}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Tag {

    /**
     * Label of it.
     * @return Label
     */
    @NotNull(message = "label is never NULL")
    String label();

    /**
     * Level.
     * @return Level
     */
    @NotNull(message = "level is never NULL")
    Level level();

    /**
     * Map of attributes.
     * @return Map of attributes
     */
    @NotNull(message = "map of attributes is never NULL")
    Map<String, String> attributes();

    /**
     * Description in Markdown (may be empty), preferably one line.
     * @return Markdown
     */
    @NotNull(message = "markdown is never NULL")
    String markdown();

    /**
     * Simple implementation.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = { "name", "lvl", "attrs", "text" })
    final class Simple implements Tag {
        /**
         * Label.
         */
        private final transient String name;
        /**
         * Level of it.
         */
        private final transient String lvl;
        /**
         * Map of attributes.
         */
        private final transient ArrayMap<String, String> attrs;
        /**
         * Markdown.
         */
        private final transient String text;
        /**
         * Public ctor.
         * @param label Label
         * @param level Level
         */
        public Simple(final String label, final Level level) {
            this(label, level, new ArrayMap<String, String>(), "");
        }
        /**
         * Public ctor.
         * @param label Label of it
         * @param level Level of tag
         * @param map Map of attributes
         * @param txt Markdown details
         * @checkstyle ParameterNumber (6 lines)
         */
        public Simple(
            @NotNull(message = "label can't be NULL") final String label,
            @NotNull(message = "level can't be NULL") final Level level,
            @NotNull(message = "map of attributes can't be NULL")
            final Map<String, String> map,
            @NotNull(message = "markdown can't be NULL") final String txt) {
            this.name = label;
            this.lvl = level.toString();
            this.attrs = new ArrayMap<String, String>(map);
            this.text = txt;
        }
        @Override
        public String toString() {
            return String.format("%s:%s", this.name, this.lvl);
        }
        @Override
        @NotNull(message = "label is never NULL")
        public String label() {
            return this.name;
        }
        @Override
        @NotNull(message = "level is never NULL")
        public Level level() {
            return Level.parse(this.lvl);
        }
        @Override
        @NotNull(message = "map of attributes is never NULL")
        public Map<String, String> attributes() {
            return this.attrs;
        }
        @Override
        @NotNull(message = "markdown is never NULL")
        public String markdown() {
            return this.text;
        }
    }

}
