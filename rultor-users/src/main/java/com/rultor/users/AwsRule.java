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
package com.rultor.users;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Item;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Rule;
import com.rultor.spi.Spec;
import com.rultor.spi.Wallet;
import java.io.IOException;
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
@EqualsAndHashCode(of = { "item", "client" })
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
     * Dynamo DB table column.
     */
    public static final String FIELD_DRAIN = "drain";

    /**
     * Dynamo DB table column.
     */
    public static final String FIELD_FAILURE = "failure";

    /**
     * Dynamo DB table column.
     */
    private static final String NO_FAILURE = "-";

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
    AwsRule(final SQSClient sqs, final Item itm) {
        this.client = sqs;
        this.item = itm;
    }

    @Override
    public void update(
        @NotNull(message = "spec can't be NULL") final Spec spec,
        @NotNull(message = "drain can't be NULL") final Spec drain) {
        try {
            this.item.put(
                new AttributeUpdates()
                    .with(AwsRule.FIELD_SPEC, spec.asText())
                    .with(AwsRule.FIELD_DRAIN, drain.asText())
                    .with(AwsRule.FIELD_FAILURE, AwsRule.NO_FAILURE)
            );
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    @NotNull(message = "spec of a rule is never NULL")
    public Spec spec() {
        final Spec spec;
        try {
            if (this.item.has(AwsRule.FIELD_SPEC)) {
                spec = new Spec.Simple(
                    this.item.get(AwsRule.FIELD_SPEC).getS()
                );
            } else {
                spec = new Spec.Simple();
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        return spec;
    }

    @Override
    public String name() {
        try {
            return this.item.get(AwsRule.RANGE_NAME).getS();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Wallet wallet(final Coordinates work,
        final URN taker, final String rule) {
        try {
            return new SQSWallet(
                this.client, work,
                this.owner(), this.name(),
                taker, rule
            );
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public Spec drain() {
        final Spec spec;
        try {
            if (this.item.has(AwsRule.FIELD_DRAIN)) {
                spec = new Spec.Simple(
                    this.item.get(AwsRule.FIELD_DRAIN).getS()
                );
            } else {
                spec = new Spec.Simple("com.rultor.drain.Trash()");
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        return spec;
    }

    @Override
    public void failure(final String desc) {
        try {
            this.item.put(
                new AttributeUpdates()
                    .with(AwsRule.FIELD_SPEC, this.spec().asText())
                    .with(AwsRule.FIELD_DRAIN, this.drain().asText())
                    .with(AwsRule.FIELD_FAILURE, desc)
            );
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String failure() {
        String failure;
        try {
            if (this.item.has(AwsRule.FIELD_FAILURE)) {
                failure = this.item.get(AwsRule.FIELD_FAILURE).getS();
                if (failure.equals(AwsRule.NO_FAILURE)) {
                    failure = "";
                }
            } else {
                failure = "";
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        return failure;
    }

    /**
     * Owner of it.
     * @return URN of the owner
     * @throws IOException If fails
     */
    private URN owner() throws IOException {
        return URN.create(this.item.get(AwsRule.HASH_OWNER).getS());
    }

}
