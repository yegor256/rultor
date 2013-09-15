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

import com.jcabi.urn.URN;
import com.rultor.spi.InvalidCouponException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import javax.sql.DataSource;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * Account in PostgreSQL.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public final class PgAccountTest {

    /**
     * Funding with wrong coupon.
     * @throws Exception In case of error.
     */
    @Test(expected = InvalidCouponException.class)
    @SuppressWarnings("PMD.CloseResource")
    public void invalidCoupon() throws Exception {
        final PgClient client = Mockito.mock(PgClient.class);
        final DataSource data = Mockito.mock(DataSource.class);
        final Connection conn = Mockito.mock(Connection.class);
        final PreparedStatement stmt = Mockito.mock(PreparedStatement.class);
        Mockito
            .when(conn.prepareStatement(Mockito.anyString())).thenReturn(stmt);
        final ResultSet result = Mockito.mock(ResultSet.class);
        Mockito.when(stmt.executeQuery()).thenReturn(result);
        Mockito.when(data.getConnection()).thenReturn(conn);
        Mockito.when(client.get()).thenReturn(data);
        new PgAccount(client, new URN()).fund("code");
    }

    /**
     * Funding with correct coupon code.
     * @throws Exception In case of error.
     * @checkstyle ExecutableStatementCountCheck (3 lines)
     */
    @Test
    @SuppressWarnings("PMD.CloseResource")
    public void correctCoupon() throws Exception {
        final String code = "newcode";
        final long amount = new Random().nextLong();
        final int pos = 6;
        final PgClient client = Mockito.mock(PgClient.class);
        final DataSource data = Mockito.mock(DataSource.class);
        final Connection conn = Mockito.mock(Connection.class);
        final PreparedStatement selectStmt =
            Mockito.mock(PreparedStatement.class);
        final PreparedStatement deleteStmt =
            Mockito.mock(PreparedStatement.class);
        final PreparedStatement updateStmt =
            Mockito.mock(PreparedStatement.class);
        Mockito.when(
            conn.prepareStatement(Mockito.startsWith("SELECT"))
        ).thenReturn(selectStmt);
        Mockito.when(
            conn.prepareStatement(Mockito.startsWith("DELETE"))
        ).thenReturn(deleteStmt);
        Mockito.when(
            conn.prepareStatement(Mockito.startsWith("INSERT"))
        ).thenReturn(updateStmt);
        final ResultSet result = Mockito.mock(ResultSet.class);
        Mockito.when(result.next()).thenReturn(true, false);
        Mockito.when(result.getLong(Mockito.eq(1))).thenReturn(amount);
        Mockito.when(selectStmt.executeQuery()).thenReturn(result);
        Mockito.when(data.getConnection()).thenReturn(conn);
        Mockito.when(client.get()).thenReturn(data);
        new PgAccount(client, new URN()).fund(code);
        Mockito.verify(selectStmt).setString(Matchers.eq(1), Matchers.eq(code));
        Mockito.verify(deleteStmt).setString(Matchers.eq(1), Matchers.eq(code));
        Mockito.verify(updateStmt)
            .setLong(Matchers.eq(pos), Matchers.eq(amount));
    }
}
