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
import com.jcabi.immutable.ArraySortedSet;
import com.jcabi.jdbc.JdbcSession;
import com.jcabi.urn.URN;
import com.rultor.spi.Pageable;
import com.rultor.spi.Sheet;
import com.rultor.tools.Time;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Sheet in PostgreSQL.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "client", "owner", "orders", "groups" })
@Loggable(Loggable.DEBUG)
final class PgSheet implements Sheet {

    /**
     * PostgreSQL client.
     */
    private final transient PgClient client;

    /**
     * Owner.
     */
    private final transient URN owner;

    /**
     * Order by.
     */
    private final transient ArraySortedSet<String> orders;

    /**
     * Group by.
     */
    private final transient ArraySortedSet<String> groups;

    /**
     * Public ctor.
     * @param clnt Client
     * @param urn URN of the owner
     */
    protected PgSheet(final PgClient clnt, final URN urn) {
        this(clnt, urn, new ArrayList<String>(0), new ArrayList<String>(0));
    }

    /**
     * Public ctor.
     * @param clnt Client
     * @param urn URN of the owner
     * @param ords Orders
     * @param grps Groups
     * @checkstyle ParameterNumber (5 lines)
     */
    protected PgSheet(final PgClient clnt, final URN urn,
        final Collection<String> ords, final Collection<String> grps) {
        this.client = clnt;
        this.owner = urn;
        this.orders = new ArraySortedSet<String>(
            ords, new ArraySortedSet.Comparator.Default<String>()
        );
        this.groups = new ArraySortedSet<String>(
            grps, new ArraySortedSet.Comparator.Default<String>()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> columns() {
        try {
            return new JdbcSession(this.client.get())
                // @checkstyle LineLength (1 line)
                .sql("SELECT column_name FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = 'receipt'")
                .select(
                    new JdbcSession.Handler<List<String>>() {
                        @Override
                        public List<String> handle(final ResultSet rset)
                            throws SQLException {
                            final List<String> names = new LinkedList<String>();
                            while (rset.next()) {
                                names.add(rset.getString(1));
                            }
                            return names;
                        }
                    }
                );
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet orderBy(final String column) {
        return new PgSheet(
            this.client, this.owner, this.orders.with(column), this.groups
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet groupBy(final String column) {
        return new PgSheet(
            this.client, this.owner, this.orders, this.groups.with(column)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<List<Object>, Integer> tail(final Integer head)
        throws IOException {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<List<Object>> iterator() {
        final StringBuilder query = new StringBuilder();
        query.append("SELECT ")
            .append(StringUtils.join(this.columns(), ","))
            .append(" FROM receipt")
            .append(" WHERE ct = ? OR dt = ?");
        if (!this.groups.isEmpty()) {
            query.append(" GROUP BY ")
                .append(StringUtils.join(this.groups, ','));
        }
        if (!this.orders.isEmpty()) {
            query.append(" ORDER BY ")
                .append(StringUtils.join(this.orders, ','));
        }
        try {
            return new JdbcSession(this.client.get())
                .sql(query.toString())
                .set(this.owner)
                .set(this.owner)
                .select(
                    new JdbcSession.Handler<Collection<List<Object>>>() {
                        @Override
                        public Collection<List<Object>> handle(
                            final ResultSet rset) throws SQLException {
                            final Collection<List<Object>> rows =
                                new LinkedList<List<Object>>();
                            while (rset.next()) {
                                rows.add(PgSheet.toRow(rset));
                            }
                            return rows;
                        }
                    }
                )
                .iterator();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet between(final Time left, final Time right) {
        throw new UnsupportedOperationException();
    }

    /**
     * Convert result set to a row of objects.
     * @param rset Result set
     * @return Row of objects
     * @throws SQLException If fails
     */
    private static List<Object> toRow(final ResultSet rset)
        throws SQLException {
        final int total = rset.getMetaData().getColumnCount();
        final List<Object> row = new ArrayList<Object>(total);
        for (int idx = 1; idx < total + 1; ++idx) {
            row.add(rset.getObject(idx));
        }
        return row;
    }

}
