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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.SingleHandler;
import com.jcabi.urn.URN;
import com.rultor.spi.Account;
import com.rultor.spi.Sheet;
import com.rultor.tools.Dollars;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Account in PostgreSQL.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "client")
@Loggable(Loggable.DEBUG)
final class PgAccount implements Account {

    /**
     * PostgreSQL client.
     */
    private final transient PgClient client;

    /**
     * Owner of the account.
     */
    private final transient URN owner;

    /**
     * Public ctor.
     * @param clnt Client
     * @param urn Owner of the account
     */
    protected PgAccount(final PgClient clnt, final URN urn) {
        this.client = clnt;
        this.owner = urn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dollars balance() {
        try {
            final long credit = new JdbcSession(this.client.get())
                .sql("SELECT SUM(amount) FROM receipt WHERE ct=?")
                .set(this.owner)
                .select(new SingleHandler<Long>(Long.class));
            final long debit = new JdbcSession(this.client.get())
                .sql("SELECT SUM(amount) FROM receipt WHERE dt=?")
                .set(this.owner)
                .select(new SingleHandler<Long>(Long.class));
            return new Dollars(debit - credit);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet sheet() {
        return new PgSheet(this.client, this.owner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fund(final Dollars amount, final String details) {
        try {
            new JdbcSession(this.client.get())
                // @checkstyle LineLength (1 line)
                .sql("INSERT INTO receipt (ct, ctunit, dt, dtunit, details, amount) VALUES (?, ?, ?, ?, ?, ?)")
                .set("urn:rultor:1")
                .set("")
                .set(this.owner)
                .set("")
                .set(details)
                .set(amount.points())
                .update();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
