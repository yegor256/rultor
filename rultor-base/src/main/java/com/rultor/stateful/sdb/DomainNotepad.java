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

import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.aws.SDBClient;
import com.rultor.spi.Dollars;
import com.rultor.spi.Expense;
import com.rultor.spi.Work;
import com.rultor.stateful.Notepad;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * SimpleDB notepads.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "client", "work" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class DomainNotepad implements Notepad {

    /**
     * Attribute name for the owner.
     */
    private static final String ATTR_OWNER = "owner";

    /**
     * Attribute name for the unit.
     */
    private static final String ATTR_UNIT = "unit";

    /**
     * Attribute name for the text.
     */
    private static final String ATTR_TEXT = "text";

    /**
     * SimpleDB client.
     */
    private final transient SDBClient client;

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Public ctor.
     * @param wrk Work
     * @param clnt Client
     */
    public DomainNotepad(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "SimpleDB client can't be NULL")
        final SDBClient clnt) {
        this.work = wrk;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "SimpleDB notepads in `%s` accessed with %s",
            this.client.domain(), this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return Iterators.size(this.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return Iterators.size(this.iterator()) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object object) {
        return Iterators.contains(this.iterator(), object);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> iterator() {
        final String query = String.format(
            "SELECT `%s` FROM `%s` WHERE `%s` = '%s' AND `%s` = '%s'",
            DomainNotepad.ATTR_TEXT,
            this.client.domain(),
            DomainNotepad.ATTR_OWNER,
            this.work.owner(),
            DomainNotepad.ATTR_UNIT,
            this.work.unit()
        );
        final SelectResult result = this.client.get().select(
            new SelectRequest()
                .withConsistentRead(true)
                .withSelectExpression(query)
        );
        this.work.spent(
            new Expense.Simple(
                String.format(
                    "retrieved AWS SimpleDB %d items from '%s' domain",
                    result.getItems().size(),
                    this.client.domain()
                ),
                new Dollars(-Tv.HUNDRED)
            )
        );
        final Collection<String> items = new LinkedList<String>();
        for (Item item : result.getItems()) {
            items.add(item.getAttributes().get(0).getValue());
        }
        return items.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return Iterators.toArray(this.iterator(), Object.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(final T[] array) {
        return (T[]) Iterators.toArray(this.iterator(), String.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final String line) {
        this.client.get().putAttributes(
            new PutAttributesRequest()
                .withDomainName(this.client.domain())
                .withItemName(this.name(line))
                .withAttributes(
                    new ReplaceableAttribute()
                        .withName(DomainNotepad.ATTR_TEXT)
                        .withValue(line)
                        .withReplace(true),
                    new ReplaceableAttribute()
                        .withName(DomainNotepad.ATTR_OWNER)
                        .withValue(this.work.owner().toString())
                        .withReplace(true),
                    new ReplaceableAttribute()
                        .withName(DomainNotepad.ATTR_UNIT)
                        .withValue(this.work.unit())
                        .withReplace(true)
                )
        );
        this.work.spent(
            new Expense.Simple(
                String.format(
                    "added AWS SimpleDB item '%s' to '%s' domain",
                    this.name(line.toString()),
                    this.client.domain()
                ),
                new Dollars(-Tv.HUNDRED)
            )
        );
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final Object line) {
        this.client.get().deleteAttributes(
            new DeleteAttributesRequest()
                .withDomainName(this.client.domain())
                .withItemName(this.name(line.toString()))
        );
        this.work.spent(
            new Expense.Simple(
                String.format(
                    "removed AWS SimpleDB item '%s' from '%s' domain",
                    this.name(line.toString()),
                    this.client.domain()
                ),
                new Dollars(-Tv.HUNDRED)
            )
        );
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(final Collection<?> list) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection<? extends String> list) {
        for (String line : list) {
            this.add(line);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(final Collection<?> list) {
        for (Object line : list) {
            this.remove(line);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(final Collection<?> list) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        for (String line : this) {
            this.remove(line);
        }
    }

    /**
     * Calculate name of an item.
     * @param text Text of the item
     * @return The name (possibly unique)
     */
    private String name(final String text) {
        return DigestUtils.md5Hex(
            String.format(
                "%s %s %s",
                this.work.owner(),
                this.work.unit(),
                text
            )
        );
    }

}
