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
package com.rultor.mongo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArrayMap;
import com.rultor.spi.Time;
import com.rultor.timeline.Event;
import com.rultor.timeline.Product;
import com.rultor.timeline.Tag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Event in Mongo.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "attrs")
@Loggable(Loggable.DEBUG)
public final class MongoEvent implements Event {

    /**
     * Mongo attribute.
     */
    public static final String ATTR_TEXT = "text";

    /**
     * Mongo attribute.
     */
    public static final String ATTR_TIME = "time";

    /**
     * Data from DB.
     */
    private final transient ArrayMap<String, Object> attrs;

    /**
     * Public ctor.
     * @param map Map of attributes
     */
    @SuppressWarnings("unchecked")
    public MongoEvent(final Map<String, Object> map) {
        this.attrs = new ArrayMap<String, Object>(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Time time() {
        return new Time(
            Long.parseLong(this.attrs.get(MongoEvent.ATTR_TIME).toString())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String text() {
        return this.attrs.get(MongoEvent.ATTR_TEXT).toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Tag> tags() {
        return new ArrayList<Tag>(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Product> products() {
        return new ArrayList<Product>(0);
    }

}
