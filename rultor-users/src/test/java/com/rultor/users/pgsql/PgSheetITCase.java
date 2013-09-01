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
import com.rultor.spi.Column;
import com.rultor.spi.Sheet;
import com.rultor.tools.Time;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link PgSheet}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class PgSheetITCase {

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
     * PgSheet can show a list of columns.
     * @throws Exception If some problem inside
     */
    @Test
    public void fetchesListOfColumns() throws Exception {
        final Sheet sheet = this.sheet();
        MatcherAssert.assertThat(
            sheet.columns(),
            Matchers.hasItem(new Column.Simple("ct", true, false))
        );
    }

    /**
     * PgSheet can fetch data lines.
     * @throws Exception If some problem inside
     */
    @Test
    public void fetchesDataLines() throws Exception {
        final Sheet sheet = this.sheet();
        MatcherAssert.assertThat(
            sheet.groupBy("dt")
                .groupBy("dtrule")
                .orderBy("time", true)
                .orderBy("id", false)
                .where().equalTo("amount", "1").sheet()
                .between(
                    new Time(new Date().getTime() - TimeUnit.DAYS.toMillis(2)),
                    new Time()
                )
                .tail(2),
            Matchers.<List<Object>>iterableWithSize(
                Matchers.greaterThanOrEqualTo(0)
            )
        );
    }

    /**
     * Get sheet to test against.
     * @return Sheet to test
     * @throws Exception If some problem inside
     */
    private Sheet sheet() throws Exception {
        Assume.assumeNotNull(PgSheetITCase.URL);
        return new PgSheet(
            new PgClient.Simple(PgSheetITCase.URL, PgSheetITCase.PASSWORD),
            URN.create("urn:test:1")
        );
    }

}
