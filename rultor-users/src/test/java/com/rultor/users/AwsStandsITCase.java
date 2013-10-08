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

import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.TableMocker;
import com.jcabi.urn.URN;
import com.rultor.spi.Spec;
import com.rultor.spi.Stand;
import com.rultor.spi.Stands;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration case for {@link AwsStands}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class AwsStandsITCase {

    /**
     * TCP port of DynamoDB Local.
     */
    private static final int PORT = Integer.parseInt(
        System.getProperty("dynamodb.port")
    );

    /**
     * Region to work with.
     */
    private transient Region region;

    /**
     * Table mocker to work with.
     */
    private transient TableMocker table;

    /**
     * Assume we're online.
     * @throws Exception If fails
     */
    @Before
    public void prepare() throws Exception {
        this.region = new Region.Simple(
            new Credentials.Direct(Credentials.TEST, AwsStandsITCase.PORT)
        );
        this.table = new TableMocker(
            this.region,
            new CreateTableRequest()
                .withTableName(AwsStand.TABLE)
                .withProvisionedThroughput(
                    new ProvisionedThroughput()
                        .withReadCapacityUnits(1L)
                        .withWriteCapacityUnits(1L)
                )
                .withAttributeDefinitions(
                    new AttributeDefinition()
                        .withAttributeName(AwsStand.HASH_OWNER)
                        .withAttributeType(ScalarAttributeType.S),
                    new AttributeDefinition()
                        .withAttributeName(AwsStand.RANGE_STAND)
                        .withAttributeType(ScalarAttributeType.S)
                )
                .withKeySchema(
                    new KeySchemaElement()
                        .withAttributeName(AwsStand.HASH_OWNER)
                        .withKeyType(KeyType.HASH),
                    new KeySchemaElement()
                        .withAttributeName(AwsStand.RANGE_STAND)
                        .withKeyType(KeyType.RANGE)
                )
        );
        this.table.create();
    }

    /**
     * Assume we're online.
     * @throws Exception If fails
     */
    @After
    public void drop() throws Exception {
        this.table.drop();
    }

    /**
     * AwsStands can add stands and list them.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsStandsAndListsThem() throws Exception {
        final URN owner = new URN("urn:test:6");
        final Stands stands = new AwsStands(this.region, owner);
        final String name = "test-stand";
        stands.create(name);
        MatcherAssert.assertThat(stands.contains(name), Matchers.is(true));
        MatcherAssert.assertThat(stands.contains("abc"), Matchers.is(false));
        MatcherAssert.assertThat(
            Iterables.filter(
                stands,
                new Predicate<Stand>() {
                    @Override
                    public boolean apply(final Stand stand) {
                        return stand.name().equals(name)
                            && stand.owner().equals(owner);
                    }
                }
            ),
            Matchers.<Stand>iterableWithSize(1)
        );
        final Stand stand = stands.get(name);
        MatcherAssert.assertThat(stand.owner(), Matchers.equalTo(owner));
    }

    /**
     * AwsStands can set default attributes of a new stand.
     * @throws Exception If some problem inside
     */
    @Test
    public void setsDefaultAttributesOfNewStand() throws Exception {
        final Stands stands = new AwsStands(this.region, new URN("urn:test:7"));
        final String name = "test-rule-new";
        stands.create(name);
        final Stand stand = stands.get(name);
        MatcherAssert.assertThat(
            stand.acl(),
            Matchers.<Spec>equalTo(
                new Spec.Simple("com.rultor.acl.Prohibited()")
            )
        );
        MatcherAssert.assertThat(
            stand.widgets(),
            Matchers.<Spec>equalTo(new Spec.Simple("[]"))
        );
    }

}
