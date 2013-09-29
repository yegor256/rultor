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
package com.rultor.users.mongo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArrayMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.rultor.spi.Tag;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang.Validate;

/**
 * Tag in Mongo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "name", "lvl", "attrs", "text" })
@Loggable(Loggable.DEBUG)
final class MongoTag implements Tag {

    /**
     * MongoDB table column.
     */
    public static final String ATTR_LABEL = "label";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_LEVEL = "level";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_ATTRIBUTES = "attributes";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_MARKDOWN = "markdown";

    /**
     * Label.
     */
    private final transient String name;

    /**
     * Level of it.
     */
    private final transient String lvl;

    /**
     * Attributes in JSON.
     */
    private final transient ArrayMap<String, String> attrs;

    /**
     * Markdown.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param object The object
     */
    @SuppressWarnings("unchecked")
    protected MongoTag(final DBObject object) {
        this(
            object.get(MongoTag.ATTR_LABEL).toString(),
            Level.parse(object.get(MongoTag.ATTR_LEVEL).toString()),
            Map.class.cast(object.get(MongoTag.ATTR_ATTRIBUTES)),
            object.get(MongoTag.ATTR_MARKDOWN).toString()
        );
    }

    /**
     * Public ctor.
     * @param object The object
     */
    protected MongoTag(final String label, final Level level) {
        this(label, level, new ArrayMap<String, String>(), "");
    }

    /**
     * Public ctor.
     * @param tag Another tag
     */
    protected MongoTag(final Tag tag) {
        this(tag.label(), tag.level(), tag.attributes(), tag.markdown());
    }

    /**
     * Public ctor.
     * @param label Label
     * @param level Level
     * @param map Map of attributes
     * @param markdown Markdown
     * @checkstyle ParameterNumber (5 lines)
     */
    protected MongoTag(final String label, final Level level,
        final Map<String, String> map, final String markdown) {
        Validate.isTrue(
            label.matches("[a-z][a-z0-9-]+"),
            "invalid tag '%s'", label
        );
        this.name = label;
        this.lvl = level.toString();
        for (String attr : map.keySet()) {
            Validate.isTrue(
                attr.matches("[a-zA-Z]+"),
                "invalid attribute name '%s'", attr
            );
        }
        this.attrs = new ArrayMap<String, String>(map);
        this.text = markdown;
    }

    /**
     * Make Mongo DBObject out of it.
     * @return Object
     */
    public DBObject asObject()  {
        return new BasicDBObject()
            .append(MongoTag.ATTR_LABEL, this.label())
            .append(MongoTag.ATTR_LEVEL, this.level().toString())
            .append(MongoTag.ATTR_ATTRIBUTES, this.attrs)
            .append(MongoTag.ATTR_MARKDOWN, this.markdown());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "label is never NULL")
    public String label() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "level is never NULL")
    public Level level() {
        return Level.parse(this.lvl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "map of attributes is never NULL")
    public Map<String, String> attributes() {
        return this.attrs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "markdown is never NULL")
    public String markdown() {
        return this.text;
    }

}
