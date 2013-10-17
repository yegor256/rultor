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

import com.jcabi.aspects.Tv;
import com.jcabi.simpledb.Credentials;
import com.jcabi.simpledb.Domain;
import com.jcabi.simpledb.Region;
import com.rultor.spi.Wallet;
import com.rultor.stateful.Spinbox;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link ItemSpinbox}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class ItemSpinboxITCase {

    /**
     * SimpleDB key.
     */
    private static final String KEY =
        System.getProperty("failsafe.sdb.key");

    /**
     * SimpleDB secret.
     */
    private static final String SECRET =
        System.getProperty("failsafe.sdb.secret");

    /**
     * ItemSpinbox can run code in parallel.
     * @throws Exception If some problem inside
     */
    @Test
    public void runsInParallel() throws Exception {
        final Domain domain = this.domain();
        try {
            final Spinbox spinbox = new ItemSpinbox(
                new Wallet.Empty(), domain.item("ItemSpinboxITCase")
            );
            final long before = spinbox.add(0);
            MatcherAssert.assertThat(
                spinbox.add(1),
                Matchers.equalTo(before + 1)
            );
        } finally {
            domain.drop();
        }
    }

    /**
     * Make a domain.
     * @return The domain
     * @throws Exception If some problem inside
     */
    private Domain domain() throws Exception {
        Assume.assumeNotNull(ItemSpinboxITCase.KEY);
        final String name = String.format(
            "test-%s", RandomStringUtils.randomAlphabetic(Tv.FIVE)
        );
        final Domain domain = new Region.Simple(
            new Credentials.Simple(
                ItemSpinboxITCase.KEY,
                ItemSpinboxITCase.SECRET
            )
        ).domain(name);
        domain.create();
        return domain;
    }

}
