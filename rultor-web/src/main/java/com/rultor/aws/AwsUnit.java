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
package com.rultor.aws;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.jcabi.urn.URN;
import com.rultor.spi.Pulse;
import com.rultor.spi.Spec;
import com.rultor.spi.Unit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;
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
@EqualsAndHashCode(of = { "region", "owner", "name" })
@Loggable(Loggable.DEBUG)
final class AwsUnit implements Unit {

    /**
     * Dynamo DB table name.
     */
    public static final String TABLE = "units";

    /**
     * Dynamo DB table column.
     */
    public static final String KEY_OWNER = "owner";

    /**
     * Dynamo DB table column.
     */
    public static final String KEY_NAME = "name";

    /**
     * Dynamo DB table column.
     */
    private static final String FIELD_SPEC = "spec";

    /**
     * Dynamo DB region.
     */
    private final transient Region region;

    /**
     * S3 client.
     */
    private final transient S3Client client;

    /**
     * URN of the user.
     */
    private final transient URN owner;

    /**
     * Name of the unit.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     * @param clnt S3 client
     * @param urn URN of the user/owner
     * @param label Name of it
     * @checkstyle ParameterNumber (4 lines)
     */
    protected AwsUnit(final Region reg, final S3Client clnt,
        final URN urn, final String label) {
        this.region = reg;
        this.client = clnt;
        this.owner = urn;
        this.name = label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public SortedMap<Date, Pulse> pulses() {
        final SortedMap<Date, Pulse> pulses =
            new ConcurrentSkipListMap<Date, Pulse>(Collections.reverseOrder());
        final Collection<Key> keys = new TreeSet<Key>();
        keys.addAll(this.fetch());
        keys.addAll(Caches.INSTANCE.keys(this.owner, this.name));
        for (Key key : keys) {
            pulses.put(key.date(), new S3Pulse(key));
        }
        return Collections.unmodifiableSortedMap(pulses);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushBefore
    public void spec(@NotNull final Spec spec) {
        this.item().put(
            AwsUnit.FIELD_SPEC,
            new AttributeValue(spec.asText())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Cacheable
    public Spec spec() {
        return new Spec.Simple(this.item().get(AwsUnit.FIELD_SPEC).getS());
    }

    /**
     * Fetch dynamo item.
     * @return The item
     */
    private Item item() {
        final Table table = this.region.table(AwsUnit.TABLE);
        final Iterator<Item> items =  table.frame()
            .where(AwsUnit.KEY_OWNER, Conditions.equalTo(this.owner))
            .where(AwsUnit.KEY_NAME, Conditions.equalTo(this.name))
            .iterator();
        Item item;
        if (items.hasNext()) {
            item = items.next();
        } else {
            item = table.put(
                new Attributes()
                    .with(AwsUnit.KEY_OWNER, this.owner)
                    .with(AwsUnit.KEY_NAME, this.name)
            );
        }
        return item;
    }

    /**
     * Get all keys from S3.
     * @return All keys
     */
    private Collection<Key> fetch() {
        final AmazonS3 aws = this.client.get();
        final ListObjectsRequest request = new ListObjectsRequest()
            .withBucketName(this.client.bucket())
            .withMaxKeys(Tv.TWENTY)
            .withPrefix(String.format("%s/%s/", this.owner, this.name));
        final ObjectListing listing = aws.listObjects(request);
        final Collection<Key> keys = new LinkedList<Key>();
        for (S3ObjectSummary sum : listing.getObjectSummaries()) {
            keys.add(Key.valueOf(this.client, sum.getKey()));
        }
        return keys;
    }

}
