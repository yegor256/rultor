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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Pulses.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Pulses extends Pageable<Pulse, Coordinates> {

    /**
     * Get query to filter by.
     * @return Query to use
     */
    @NotNull(message = "query is never NULL")
    Query query();

    /**
     * One row that doesn't support paginating.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    @Loggable(Loggable.DEBUG)
    final class Row implements Pulses {
        /**
         * Encapsulated array.
         */
        private final transient com.jcabi.immutable.Array<Pulse> array;
        /**
         * Public ctor.
         */
        public Row() {
            this(new ArrayList<Pulse>(0));
        }
        /**
         * Public ctor.
         * @param data Array of data
         */
        public Row(@NotNull(message = "array can't be NULL")
            final Collection<Pulse> data) {
            this.array = new com.jcabi.immutable.Array<Pulse>(data);
        }
        @Override
        public Iterator<Pulse> iterator() {
            return this.array.iterator();
        }
        @Override
        public Pageable<Pulse, Coordinates> tail(final Coordinates head) {
            return this;
        }
        @Override
        public Query query() {
            return new Query() {
                @Override
                public Pulses fetch() {
                    return Pulses.Row.this;
                }
                @Override
                public Query withTag(final String label) {
                    return this;
                }
            };
        }
    }

}
