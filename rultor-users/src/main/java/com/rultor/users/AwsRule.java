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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Rule;
import com.rultor.spi.Spec;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Single unit in Dynamo DB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "item")
@Loggable(Loggable.DEBUG)
final class AwsRule implements Rule {

    /**
     * Dynamo DB table name (the table has this name for historical
     * reason, and we can't change it since there is no table renaming
     * feature in DynamoDB).
     */
    public static final String TABLE = "units";

    /**
     * Dynamo DB table column.
     */
    public static final String HASH_OWNER = "owner";

    /**
     * Dynamo DB table column.
     */
    public static final String RANGE_NAME = "name";

    /**
     * Dynamo DB table column.
     */
    public static final String FIELD_SPEC = "spec";

    /**
     * Item.
     */
    private final transient Item item;

    /**
     * SQS client.
     */
    private final transient SQSClient client;

    /**
     * Public ctor.
     * @param sqs SQS client
     * @param itm Item from Dynamo
     */
    protected AwsRule(final SQSClient sqs, final Item itm) {
        this.client = sqs;
        this.item = itm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushAfter
    public void update(@NotNull(message = "spec is mandatory and can't be NULL")
        final Spec spec) {
        this.item.put(
            new Attributes()
                .with(
                    AwsRule.FIELD_SPEC,
                    new AttributeValue(spec.asText())
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "spec of a rule is never NULL")
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    public Spec spec() {
        Spec spec;
        if (this.item.has(AwsRule.FIELD_SPEC)) {
            spec = new Spec.Simple(this.item.get(AwsRule.FIELD_SPEC).getS());
        } else {
            spec = new Spec.Simple();
        }
        return spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.item.get(AwsRule.RANGE_NAME).getS();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Wallet wallet(final Work work, final URN taker, final String rule) {
        return new SQSWallet(
            this.client, work,
            this.owner(), this.name(),
            taker, rule
        );
    }

    /**
     * Owner of it.
     * @return URN of the owner
     */
    private URN owner() {
        return URN.create(this.item.get(AwsRule.HASH_OWNER).getS());
    }

}
