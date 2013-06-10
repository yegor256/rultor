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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
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
public final class DynamoUsers implements Users {

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * Public ctor.
     * @param key AWS key
     * @param secret AWS secret
     * @param prefix Prefix for AWS DynamoDB tables
     */
    public DynamoUsers(final String key, final String secret,
        final String prefix) {
        this.region = new Region.Prefixed(
            new Region.Simple(new Credentials.Simple(key, secret)),
            prefix
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<User> everybody() {
        final ConcurrentMap<URN, User> users =
            new ConcurrentSkipListMap<URN, User>();
        for (Item item : this.region.table("units").frame()) {
            final URN urn = URN.create(item.get(DynamoUnit.KEY_OWNER).getS());
            if (!users.containsKey(urn)) {
                users.put(urn, this.fetch(urn));
            }
        }
        return Collections.unmodifiableCollection(users.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User fetch(final URN urn) {
        return new DynamoUser(this.region, urn);
    }

}
