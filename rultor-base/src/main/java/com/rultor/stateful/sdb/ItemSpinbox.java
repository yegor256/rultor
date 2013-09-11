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

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.rultor.aws.SDBClient;
import com.rultor.spi.Wallet;
import com.rultor.stateful.Spinbox;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Spinbox in SimpleDB item.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "client", "name" })
public final class ItemSpinbox implements Spinbox {

    /**
     * Attribute name.
     */
    private static final String COUNTER = "counter";

    /**
     * Attribute name.
     */
    private static final String TIME = "time";

    /**
     * Wallet to charge.
     */
    private final transient Wallet wallet;

    /**
     * SimpleDB client.
     */
    private final transient SDBClient client;

    /**
     * Object name.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param wlt Wallet to charge
     * @param obj Item name
     * @param clnt Client
     */
    public ItemSpinbox(
        @NotNull(message = "wallet can't be NULL") final Wallet wlt,
        @NotNull(message = "object name can't be NULL") final String obj,
        @NotNull(message = "SimpleDB client can't be NULL")
        final SDBClient clnt) {
        this.wallet = wlt;
        this.name = obj;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RetryOnFailure(verbose = false)
    public long add(final long value) {
        final AmazonSimpleDB aws = this.client.get();
        final GetAttributesResult result = aws.getAttributes(
            new GetAttributesRequest()
                .withConsistentRead(true)
                .withDomainName(this.client.domain())
                .withItemName(this.name)
                .withAttributeNames(ItemSpinbox.COUNTER)
        );
        final long before;
        if (result.getAttributes().isEmpty()) {
            before = 0;
        } else {
            before = Long.parseLong(result.getAttributes().get(0).getValue());
        }
        final long after = before + value;
        aws.putAttributes(
            new PutAttributesRequest()
                .withDomainName(this.client.domain())
                .withItemName(this.name)
                .withAttributes(
                    new ReplaceableAttribute()
                        .withName(ItemSpinbox.COUNTER)
                        .withValue(Long.toString(after))
                        .withReplace(true),
                    new ReplaceableAttribute()
                        .withName(ItemSpinbox.TIME)
                        .withValue(new Time().toString())
                        .withReplace(true)
                )
        );
        this.wallet.charge(
            String.format(
                "added %d to spinbox in AWS SimpleDB item `%s` in `%s` domain",
                value, this.name, this.client.domain()
            ),
            new Dollars(Tv.FIVE)
        );
        return after;
    }

}
