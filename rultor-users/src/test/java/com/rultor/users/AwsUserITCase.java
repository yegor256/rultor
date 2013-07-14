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

import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import com.rultor.spi.User;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration case for {@link AwsUser}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class AwsUserITCase {

    /**
     * Region to work with.
     */
    private transient Region region;

    /**
     * Assume we're online.
     */
    @Before
    public void prepare() {
        final String key = System.getProperty("failsafe.dynamo.key");
        Assume.assumeNotNull(key);
        this.region = new Region.Prefixed(
            new Region.Simple(
                new Credentials.Simple(
                    key,
                    System.getProperty("failsafe.dynamo.secret")
                )
            ),
            System.getProperty("failsafe.dynamo.prefix")
        );
    }

    /**
     * AwsUser can work with real data.
     * @throws Exception If some problem inside
     */
    @Test
    public void worksWithRealDynamoDb() throws Exception {
        final URN urn = new URN("urn:github:66");
        final User user = new AwsUser(this.region, urn);
        MatcherAssert.assertThat(user.urn(), Matchers.equalTo(urn));
        final String name = "simple-unit";
        if (user.units().contains(name)) {
            user.remove(name);
        }
        user.create(name);
        MatcherAssert.assertThat(user.units(), Matchers.hasItem(name));
    }

}
