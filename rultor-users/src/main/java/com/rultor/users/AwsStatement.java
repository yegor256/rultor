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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import com.rultor.spi.Dollars;
import com.rultor.spi.Invoice;
import com.rultor.spi.Invoices;
import com.rultor.spi.Statement;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Statement in DynamoDB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "region", "name" })
@Loggable(Loggable.DEBUG)
final class AwsStatement implements Statement {

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * URN of the user.
     */
    private final transient URN name;

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     * @param urn URN of the user
     */
    protected AwsStatement(final Region reg, final URN urn) {
        this.region = reg;
        this.name = urn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable
    public Invoices invoices() {
        return new AwsInvoices(this.region, this.name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable
    public Dollars balance() {
        final AmazonDynamoDB aws = this.region.aws();
        final QueryResult result = aws.query(
            new QueryRequest()
                .withConsistentRead(true)
                .withLimit(1)
                .withScanIndexForward(false)
                .withTableName(this.region.table(AwsInvoices.TABLE).name())
                .withKeyConditions(
                    new Conditions().with(
                        AwsInvoices.KEY_OWNER,
                        Conditions.equalTo(this.name)
                    )
                )
                .withAttributesToGet(AwsInvoices.FIELD_BALANCE)
        );
        final long balance;
        if (result.getCount() == 0) {
            balance = 0;
        } else {
            balance = Long.parseLong(
                result.getItems().get(0).get(AwsInvoices.FIELD_BALANCE).getN()
            );
        }
        return new Dollars(balance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushAfter
    public void add(final Invoice invoice) {
        final Dollars balance = this.balance();
        this.region.table(AwsInvoices.TABLE).put(
            new Attributes()
                .with(AwsInvoices.KEY_OWNER, this.name)
                .with(AwsInvoices.KEY_CODE, new Invoice.Code(invoice))
                .with(AwsInvoices.FIELD_TEXT, invoice.text())
                .with(
                    AwsInvoices.FIELD_AMOUNT,
                    new AttributeValue().withN(
                        Long.toString(invoice.amount().points())
                    )
                )
                .with(
                    AwsInvoices.FIELD_BALANCE,
                    new AttributeValue().withN(
                        Long.toString(
                            balance.points() + invoice.amount().points()
                        )
                    )
                )
        );
    }

}
