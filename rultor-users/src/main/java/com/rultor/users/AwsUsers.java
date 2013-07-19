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
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableResult;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.rultor.spi.Metricable;
import com.rultor.spi.Receipt;
import com.rultor.spi.Statement;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * All users in Dynamo DB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "region")
@Loggable(Loggable.DEBUG)
public final class AwsUsers implements Users, Metricable {

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * Public ctor.
     * @param reg AWS region
     */
    public AwsUsers(final Region reg) {
        final AmazonDynamoDB aws = reg.aws();
        final DescribeTableResult result = aws.describeTable(
            new DescribeTableRequest()
                // @checkstyle MultipleStringLiterals (1 line)
                .withTableName(reg.table("units").name())
        );
        Logger.info(
            AwsUsers.class, "Amazon DynamoDB is ready with %d units",
            result.getTable().getItemCount()
        );
        this.region = reg;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "list of URN names is never NULL")
    public Set<URN> everybody() {
        final Set<URN> users = new HashSet<URN>(0);
        for (Item item : this.region.table("units").frame()) {
            users.add(URN.create(item.get(AwsUnit.KEY_OWNER).getS()));
        }
        return Collections.unmodifiableSet(users);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "User is never NULL")
    public User get(@NotNull(message = "URN can't be empty") final URN urn) {
        return new AwsUser(this.region, urn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void charge(@NotNull(message = "receipt can't be empty")
        final Receipt receipt) {
        AwsReceipts.add(this.region, receipt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reconcile() {
        final ConcurrentMap<URN, Statement> statements =
            new AwsPending(this.region).fetch();
        for (Map.Entry<URN, Statement> entry : statements.entrySet()) {
            this.get(entry.getKey()).statements().add(entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final MetricRegistry registry) {
        registry.register(
            MetricRegistry.name(this.getClass(), "users-total"),
            new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return AwsUsers.this.everybody().size();
                }
            }
        );
    }

}
