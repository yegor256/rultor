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
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Stand;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
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
public final class AwsUsers implements Users {

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * SQS client.
     */
    private final transient SQSClient client;

    /**
     * Public ctor.
     * @param reg AWS region
     * @param sqs SQS client
     */
    public AwsUsers(final Region reg, final SQSClient sqs) {
        final AmazonDynamoDB aws = reg.aws();
        final DescribeTableResult result = aws.describeTable(
            new DescribeTableRequest()
                // @checkstyle MultipleStringLiterals (1 line)
                .withTableName(reg.table(AwsRule.TABLE).name())
        );
        Logger.info(
            AwsUsers.class, "Amazon DynamoDB is ready with %d rule(s)",
            result.getTable().getItemCount()
        );
        this.region = reg;
        this.client = sqs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "list of users is never NULL")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterator<User> iterator() {
        final Collection<User> users = new HashSet<User>(0);
        for (Item item : this.region.table(AwsRule.TABLE).frame()) {
            users.add(
                new AwsUser(
                    AwsUsers.this.region,
                    AwsUsers.this.client,
                    URN.create(item.get(AwsRule.HASH_OWNER).getS())
                )
            );
        }
        return users.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "User is never NULL")
    public User get(@NotNull(message = "URN can't be empty") final URN urn) {
        return new AwsUser(this.region, this.client, urn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    public Stand stand(final String stand) {
        final Collection<Item> items = this.region.table(AwsStand.TABLE)
            .frame()
            .where(AwsStand.RANGE_STAND, stand);
        if (items.isEmpty()) {
            throw new NoSuchElementException(
                String.format("Stand `%s` doesn't exist", stand)
            );
        }
        return new AwsStand(items.iterator().next());
    }

}
