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
package com.rultor.users.pgsql;

import com.jcabi.aspects.Parallel;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.spi.Account;
import com.rultor.tools.Dollars;
import java.security.SecureRandom;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration case for {@link PgAccount}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Ignore
public final class PgAccountITCase {

    /**
     * Randomizer.
     */
    private static final Random RND = new SecureRandom();

    /**
     * JDBC URL.
     */
    private static final String URL =
        System.getProperty("failsafe.pgsql.jdbc");

    /**
     * JDBC password.
     */
    private static final String PASSWORD =
        System.getProperty("failsafe.pgsql.password");

    /**
     * JDBC username.
     */
    private static final String USERNAME =
        System.getProperty("failsafe.pgsql.username");

    /**
     * PgAccount can fetch balance.
     * @throws Exception If some problem inside
     */
    @Test
    public void fetchesAccountBalance() throws Exception {
        final Account account = this.account();
        MatcherAssert.assertThat(
            account.balance().points(),
            Matchers.equalTo(0L)
        );
    }

    /**
     * PgAccount can fund itself.
     * @throws Exception If some problem inside
     */
    @Test
    public void fundsItself() throws Exception {
        final Account account = this.account();
        final long amount = PgAccountITCase.RND.nextInt();
        account.fund(new Dollars(amount), "service");
        MatcherAssert.assertThat(
            account.balance().points(),
            Matchers.equalTo(amount)
        );
    }

    /**
     * PgAccount can fund itself in multiple parallel threads.
     * @throws Exception If some problem inside
     */
    @Test
    public void fundsItselfInParallelThreads() throws Exception {
        final Account account = this.account();
        final long amount = PgAccountITCase.RND.nextInt();
        this.fund(account, new Dollars(amount));
        MatcherAssert.assertThat(
            account.balance().points(),
            Matchers.equalTo(amount * Tv.TEN)
        );
    }

    /**
     * Fund the account.
     * @param acc Account to fund
     * @param amount Amount to add
     */
    @Parallel(threads = Tv.TEN)
    private void fund(final Account acc, final Dollars amount) {
        acc.fund(amount, "something");
    }

    /**
     * Get account to test against.
     * @return Account to test
     * @throws Exception If some problem inside
     */
    private Account account() throws Exception {
        Assume.assumeNotNull(PgAccountITCase.URL);
        final Account account = new PgAccount(
            new PgClient.Simple(
                PgAccountITCase.URL,
                PgAccountITCase.PASSWORD, PgAccountITCase.USERNAME
            ),
            URN.create("urn:test:1")
        );
        final long now = account.balance().points();
        if (now != 0) {
            account.fund(new Dollars(-now), "zeroing it");
        }
        MatcherAssert.assertThat(
            account.balance().points(),
            Matchers.equalTo(0L)
        );
        return account;
    }

}
