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
package com.rultor.timeline;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.rultor.spi.Time;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Event in timeline.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Event {

    /**
     * When did it happen.
     * @return Time of it
     */
    Time time();

    /**
     * Text of it.
     * @return Text of the event
     */
    String text();

    /**
     * Collection of tags.
     * @return Tags
     */
    Collection<Tag> tags();

    /**
     * Products.
     * @return Products
     */
    Collection<Product> products();

    /**
     * Simple implementation.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = { "when", "txt", "tgs", "pds" })
    @Loggable(Loggable.DEBUG)
    final class Simple implements Event {
        /**
         * Time.
         */
        private final transient Time when;
        /**
         * Text of it.
         */
        private final transient String txt;
        /**
         * Tags.
         */
        private final transient Array<Tag> tgs;
        /**
         * Products.
         */
        private final transient Array<Product> pds;
        /**
         * Public ctor.
         * @param time Time
         * @param text Text
         * @param tags Tags
         * @param products Products
         * @checkstyle ParameterNumber (4 lines)
         */
        public Simple(final Time time, final String text,
            final Collection<Tag> tags, final Collection<Product> products) {
            this.when = time;
            this.txt = text;
            this.tgs = new Array<Tag>(tags);
            this.pds = new Array<Product>(products);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Time time() {
            return this.when;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String text() {
            return this.txt;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<Tag> tags() {
            return this.tgs;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<Product> products() {
            return this.pds;
        }
    }

}
