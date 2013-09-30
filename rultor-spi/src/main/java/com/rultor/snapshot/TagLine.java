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
package com.rultor.snapshot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.xembly.Directives;

/**
 * TagLine in Xembly.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@EqualsAndHashCode
public final class TagLine {

    /**
     * TagLine name.
     */
    private final transient String name;

    /**
     * Attributes.
     */
    private final transient ConcurrentMap<String, String> attrs =
        new ConcurrentHashMap<String, String>(0);

    /**
     * Log level.
     */
    private transient Level lvl = Level.INFO;

    /**
     * Markdown.
     */
    private transient String mdwn = "";

    /**
     * Public ctor.
     * @param tag Xembly directives to encapsulate
     */
    public TagLine(final String tag) {
        this.name = tag;
    }

    /**
     * Add attribute.
     * @param attr Attribute name
     * @param value Attribute value
     * @return This object
     */
    public TagLine attr(@NotNull final String attr, final String value) {
        if (value == null) {
            this.attrs.put(attr, "NULL");
        } else {
            this.attrs.put(attr, value);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.directives().toString();
    }

    /**
     * Set its level.
     * @param level Level to set
     * @return This object
     */
    public TagLine level(@NotNull final Level level) {
        this.lvl = level;
        return this;
    }

    /**
     * Set level to FINE if true or SEVERE otherwise.
     * @param success Set to FINE
     * @return This object
     */
    public TagLine fine(final boolean success) {
        if (success) {
            this.level(Level.FINE);
        } else {
            this.level(Level.SEVERE);
        }
        return this;
    }

    /**
     * Describe it in markdown.
     * @param txt The text to set
     * @return This object
     */
    public TagLine markdown(@NotNull final String txt) {
        this.mdwn = txt;
        return this;
    }

    /**
     * Log it.
     */
    public void log() {
        new XemblyLine(this.directives()).log();
    }

    /**
     * Build Xembly directives.
     * @return Directives
     */
    private Directives directives() {
        final Directives dirs = new Directives()
            .xpath("/snapshot").addIf("tags").add("tag")
            .add("label").set(this.name).up()
            .add("markdown").set(this.mdwn).up()
            .add("level").set(this.lvl.toString()).up()
            .add("attributes");
        for (Map.Entry<String, String> entry : this.attrs.entrySet()) {
            dirs.add("attribute")
                .add("name").set(entry.getKey()).up()
                .add("value").set(entry.getValue()).up().up();
        }
        return dirs;
    }

}
