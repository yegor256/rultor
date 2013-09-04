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

import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.google.common.collect.Iterators;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rultor.aws.SDBClient;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.stateful.Notepad;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * SimpleDB {@link Notepad}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "client", "work", "wallet" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class DomainNotepad implements Notepad {

    /**
     * Attribute name for the owner.
     */
    private static final String ATTR_OWNER = "owner";

    /**
     * Attribute name for the rule.
     */
    private static final String ATTR_RULE = "rule";

    /**
     * Attribute name for the text.
     */
    private static final String ATTR_TEXT = "text";

    /**
     * Attribute name for the text.
     */
    private static final String ATTR_TIME = "time";

    /**
     * SimpleDB client.
     */
    private final transient SDBClient client;

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Wallet to charge.
     */
    private final transient Wallet wallet;

    /**
     * Public ctor.
     * @param wrk Work
     * @param wlt Wallet to charge
     * @param clnt Client
     */
    public DomainNotepad(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "wallet can't be NULL") final Wallet wlt,
        @NotNull(message = "SimpleDB client can't be NULL")
        final SDBClient clnt) {
        this.work = wrk;
        this.wallet = wlt;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "SimpleDB notepad in `%s` accessed with %s",
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
        final long start = System.currentTimeMillis();
        final GetAttributesResult result = this.client.get().getAttributes(
            new GetAttributesRequest()
                .withDomainName(this.client.domain())
                .withItemName(this.name(object.toString()))
                .withConsistentRead(true)
        );
        this.wallet.charge(
            Logger.format(
                // @checkstyle LineLength (1 line)
                "checked existence of AWS SimpleDB item from `%s` domain in %[ms]s",
                this.client.domain(),
                System.currentTimeMillis() - start
            ),
            new Dollars(Tv.FIVE)
        );
        return !result.getAttributes().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RetryOnFailure
    public Iterator<String> iterator() {
        final String query = String.format(
            "SELECT `%s` FROM `%s` WHERE `%s`='%s' AND `%s`='%s'",
            DomainNotepad.ATTR_TEXT,
            this.client.domain(),
            DomainNotepad.ATTR_OWNER,
            this.work.owner(),
            DomainNotepad.ATTR_RULE,
            this.work.rule()
        );
        final long start = System.currentTimeMillis();
        final SelectResult result = this.client.get().select(
            new SelectRequest()
                .withConsistentRead(true)
                .withSelectExpression(query)
        );
        final Collection<String> items = new LinkedList<String>();
        for (Item item : result.getItems()) {
            final String text = item.getAttributes().get(0).getValue();
            if (item.getName().equals(this.name(text))) {
                items.add(text);
            }
        }
        this.wallet.charge(
            Logger.format(
                // @checkstyle LineLength (1 line)
                "retrieved AWS SimpleDB %d item(s) from `%s` domain in %[ms]s (%d total)",
                items.size(), this.client.domain(),
                System.currentTimeMillis() - start, result.getItems().size()
            ),
            new Dollars(Tv.FIVE)
        );
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
    @RetryOnFailure
    public boolean add(final String line) {
        final long start = System.currentTimeMillis();
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
                        .withName(DomainNotepad.ATTR_RULE)
                        .withValue(this.work.rule())
                        .withReplace(true),
                    new ReplaceableAttribute()
                        .withName(DomainNotepad.ATTR_TIME)
                        .withValue(new Time().toString())
                        .withReplace(true)
                )
        );
        this.wallet.charge(
            Logger.format(
                "added AWS SimpleDB item `%s` to `%s` domain in %[ms]s",
                this.name(line),
                this.client.domain(),
                System.currentTimeMillis() - start
            ),
            new Dollars(Tv.FIVE)
        );
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RetryOnFailure
    public boolean remove(final Object line) {
        final long start = System.currentTimeMillis();
        this.client.get().deleteAttributes(
            new DeleteAttributesRequest()
                .withDomainName(this.client.domain())
                .withItemName(this.name(line.toString()))
                .withAttributes(
                    new Attribute().withName(DomainNotepad.ATTR_OWNER),
                    new Attribute().withName(DomainNotepad.ATTR_TEXT),
                    new Attribute().withName(DomainNotepad.ATTR_TIME),
                    new Attribute().withName(DomainNotepad.ATTR_RULE)
                )
        );
        this.wallet.charge(
            Logger.format(
                "removed AWS SimpleDB item `%s` from `%s` domain in %[ms]s",
                this.name(line.toString()),
                this.client.domain(),
                System.currentTimeMillis() - start
            ),
            new Dollars(Tv.FIVE)
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
                this.work.rule(),
                text
            )
        );
    }

}
