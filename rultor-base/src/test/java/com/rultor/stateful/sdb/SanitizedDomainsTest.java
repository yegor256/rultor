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

package com.rultor.stateful.sdb;

import com.amazonaws.services.simpledb.model.SelectRequest;
import com.jcabi.simpledb.Domain;
import com.jcabi.simpledb.Item;
import com.jcabi.simpledb.Region;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link com.rultor.stateful.sdb.SanitizedDomains}.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.CyclomaticComplexity" })
public final class SanitizedDomainsTest {

    /**
     * SanitizedDomains filters items by time.
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void filtersOldItems() {
        final Domain dmn = Mockito.mock(Domain.class);
        Mockito.when(dmn.select(Mockito.any(SelectRequest.class)))
            .thenReturn(
                Arrays.asList(
                    // @checkstyle MultipleStringLiterals (3 line)
                    create("item1", "2000"),
                    create("item2", "3000"),
                    create("item3", "1000"),
                    create("item4", "2500")
                )
            );
        final Region region = Mockito.mock(Region.class);
        Mockito.when(region.domain(Mockito.any(String.class))).thenReturn(dmn);
        final Set<String> set = new HashSet<String>();
        // @checkstyle MagicNumberCheck (1 lines)
        for (Item item : new SanitizedDomains(region, 2300).domain("")
            .select(new SelectRequest())) {
            set.add(item.name());
        }
        MatcherAssert.assertThat(
            set,
            Matchers.<Set<String>>allOf(
                Matchers.hasSize(2),
                Matchers.hasItems("item1", "item3")
            )
        );
    }

    /**
     * Create the item with given name and time.
     * @param name The name of item
     * @param time The time of item
     * @return Wrapped one
     */
    private Item create(final String name, final String time) {
        // @checkstyle AnonInnerLength (60 lines)
        return new Item() {
            @Override
            public String name() {
                return name;
            }
            @Override
            public int size() {
                throw new UnsupportedOperationException();
            }
            @Override
            public boolean isEmpty() {
                throw new UnsupportedOperationException();
            }
            @Override
            public boolean containsKey(final Object key) {
                throw new UnsupportedOperationException();
            }
            @Override
            public boolean containsValue(final Object value) {
                throw new UnsupportedOperationException();
            }
            @Override
            public String get(final Object key) {
                return time;
            }
            @Override
            public String put(final String key, final String value) {
                throw new UnsupportedOperationException();
            }
            @Override
            public String remove(final Object key) {
                throw new UnsupportedOperationException();
            }
            @Override
            public void putAll(
                final Map<? extends String, ? extends String> map) {
                throw new UnsupportedOperationException();
            }
            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }
            @Override
            public Set<String> keySet() {
                throw new UnsupportedOperationException();
            }
            @Override
            public Collection<String> values() {
                throw new UnsupportedOperationException();
            }
            @Override
            public Set<Map.Entry<String, String>> entrySet() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
