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
import com.jcabi.immutable.Array;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Tags.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Tags extends Collection<Tag> {

    /**
     * Does it contain a tag with this label?
     * @param label Label to check
     * @return TRUE if contains
     */
    boolean contains(@NotNull(message = "label can't be NULL") String label);

    /**
     * Get tag by this label.
     * @param label Label to check
     * @return Tag found (runtime exception if not found)
     */
    Tag get(@NotNull(message = "label can't be NULL") String label);

    /**
     * Simple implementation.
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(callSuper = false, of = "list")
    final class Simple extends AbstractCollection<Tag> implements Tags {
        /**
         * Encapsulated list of tags.
         */
        private final transient Array<Tag> list;
        /**
         * Public ctor.
         * @param tags Tags to encapsulate
         */
        public Simple(final Collection<Tag> tags) {
            this.list = new Array<Tag>(tags);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Tag> iterator() {
            return this.list.iterator();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return this.list.size();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(final String label) {
            boolean contains = false;
            for (Tag tag : this.list) {
                if (tag.label().equals(label)) {
                    contains = true;
                    break;
                }
            }
            return contains;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Tag get(final String label) {
            Tag found = null;
            for (Tag tag : this.list) {
                if (tag.label().equals(label)) {
                    found = tag;
                    break;
                }
            }
            if (found == null) {
                throw new NoSuchElementException(
                    String.format("tag `%s` not found", label)
                );
            }
            return found;
        }
    }

}
