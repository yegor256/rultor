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
package com.rultor.stateful;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import lombok.EqualsAndHashCode;

/**
 * Concurrent notepad.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface ConcurrentNotepad extends Notepad {

    /**
     * Add item if it is absent.
     * @param item The item to add
     * @return TRUE if it was added
     */
    boolean addIf(String item);

    /**
     * Composite.
     */
    @Immutable
    @EqualsAndHashCode(of = { "notepad", "lineup" })
    @Loggable(Loggable.DEBUG)
    final class Composite implements ConcurrentNotepad {
        /**
         * Original notepad.
         */
        private final transient Notepad notepad;
        /**
         * Original lineup.
         */
        private final transient Lineup lineup;
        /**
         * Public ctor.
         * @param ntp Notepad
         * @param lnp Lineup
         */
        public Composite(final Notepad ntp, final Lineup lnp) {
            this.notepad = ntp;
            this.lineup = lnp;
        }
        @Override
        @SuppressWarnings("PMD.AvoidCatchingGenericException")
        public boolean addIf(final String item) {
            try {
                return this.lineup.exec(
                    new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            final boolean added;
                            if (ConcurrentNotepad.Composite.this
                                .contains(item)) {
                                added = false;
                            } else {
                                ConcurrentNotepad.Composite.this.add(item);
                                added = true;
                            }
                            return added;
                        }
                    }
                );
            // @checkstyle IllegalCatch (1 line)
            } catch (final Exception ex) {
                throw new IllegalStateException(ex);
            }
        }
        @Override
        public int size() {
            return this.notepad.size();
        }
        @Override
        public boolean isEmpty() {
            return this.notepad.isEmpty();
        }
        @Override
        public boolean contains(final Object obj) {
            return this.notepad.contains(obj);
        }
        @Override
        public Iterator<String> iterator() {
            return this.notepad.iterator();
        }
        @Override
        public Object[] toArray() {
            return this.notepad.toArray();
        }
        @Override
        public <T> T[] toArray(final T[] array) {
            return this.notepad.toArray(array);
        }
        @Override
        public boolean add(final String item) {
            return this.notepad.add(item);
        }
        @Override
        public boolean remove(final Object item) {
            return this.notepad.remove(item);
        }
        @Override
        public boolean containsAll(final Collection<?> items) {
            return this.notepad.containsAll(items);
        }
        @Override
        public boolean addAll(final Collection<? extends String> items) {
            return this.notepad.addAll(items);
        }
        @Override
        public boolean removeAll(final Collection<?> items) {
            return this.notepad.removeAll(items);
        }
        @Override
        public boolean retainAll(final Collection<?> items) {
            return this.notepad.retainAll(items);
        }
        @Override
        public void clear() {
            this.notepad.clear();
        }
    }

}
