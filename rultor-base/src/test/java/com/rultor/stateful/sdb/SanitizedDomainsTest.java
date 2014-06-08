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
import com.rultor.tools.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
        final Set<String> cleared = new HashSet<String>();
        final List<Item> list = new ArrayList<Item>(
            Arrays.asList(
                // @checkstyle MultipleStringLiterals (6 lines)
                // @checkstyle MagicNumberCheck (5 lines)
                this.create("item1", 2000, cleared),
                this.create("item2", 3000, cleared),
                this.create("item3", 1000, cleared),
                this.create("item4", 2500, cleared)
            )
        );
        Mockito.when(dmn.select(Mockito.any(SelectRequest.class)))
            .thenReturn(list);
        final Region region = Mockito.mock(Region.class);
        Mockito.when(region.domain(Mockito.any(String.class))).thenReturn(dmn);
        final Set<String> set = new HashSet<String>();
        // @checkstyle MagicNumberCheck (1 line)
        for (final Item item : new SanitizedDomains(region, 2300).domain("")
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
        MatcherAssert.assertThat(
            cleared,
            Matchers.<Set<String>>allOf(
                Matchers.hasSize(2),
                Matchers.hasItems("item2", "item4")
            )
        );
    }

    /**
     * Create the item with given name and age.
     *
     * @param name The name of item
     * @param age The age of item in minutes
     * @param cleared List of cleared items
     * @return Wrapped one
     */
    private Item create(
        final String name,
        final long age,
        final Set<String> cleared) {
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
                return new Time(
                    System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(age)
                ).toString();
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
                cleared.add(name);
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
