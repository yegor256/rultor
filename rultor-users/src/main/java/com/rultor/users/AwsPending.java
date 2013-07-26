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
package com.rultor.users;

import com.google.common.collect.ImmutableList;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import com.rultor.spi.Receipt;
import com.rultor.spi.Statement;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.CharUtils;

/**
 * Pending statements in DynamoDB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "region")
@Loggable(Loggable.DEBUG)
final class AwsPending {

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     */
    protected AwsPending(final Region reg) {
        this.region = reg;
    }

    /**
     * Fetch all pending statements.
     * @return Map of them
     */
    public ConcurrentMap<URN, Statement> fetch() {
        final ConcurrentMap<URN, Statement> stmts =
            new ConcurrentHashMap<URN, Statement>(0);
        for (Map.Entry<URN, Collection<Receipt>> entry
            : this.receipts().entrySet()) {
            stmts.put(
                entry.getKey(),
                this.make(entry.getKey(), entry.getValue())
            );
        }
        return stmts;
    }

    /**
     * Make a statement from a list of receipts.
     * @param payer Who will pay it
     * @param receipts Receipts
     * @return The statement
     */
    private Statement make(final URN payer,
        final Collection<Receipt> receipts) {
        final StringBuilder text = new StringBuilder();
        long amount = 0;
        for (Receipt rcpt : receipts) {
            text.append(rcpt.details())
                .append(' ')
                .append(rcpt.dollars())
                .append(CharUtils.LF);
            final int sign;
            if (rcpt.payer().equals(payer)) {
                sign = -1;
            } else {
                sign = 1;
            }
            amount += sign * rcpt.dollars().points();
        }
        return new Statement.Simple(
            new Time(),
            new Dollars(amount),
            text.toString()
        );
    }

    /**
     * Group all receipts into map.
     * @return Map of them
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Map<URN, Collection<Receipt>> receipts() {
        final ConcurrentMap<URN, Collection<Receipt>> receipts =
            new ConcurrentHashMap<URN, Collection<Receipt>>(0);
        for (Receipt receipt : ImmutableList.copyOf(this.all())) {
            receipts.putIfAbsent(
                receipt.beneficiary(),
                new LinkedList<Receipt>()
            );
            receipts.get(receipt.beneficiary()).add(receipt);
            receipts.putIfAbsent(
                receipt.payer(),
                new LinkedList<Receipt>()
            );
            receipts.get(receipt.payer()).add(receipt);
        }
        return receipts;
    }

    /**
     * Fetch all receipts.
     * @return All of them
     */
    private Iterator<Receipt> all() {
        final Iterator<Item> items = this.region.table(AwsReceipts.TABLE)
            .frame().iterator();
        return new Iterator<Receipt>() {
            @Override
            public boolean hasNext() {
                return items.hasNext();
            }
            @Override
            public Receipt next() {
                return AwsReceipts.toReceipt(items.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
