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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.immutable.ArraySortedSet;
import com.jcabi.jdbc.JdbcSession;
import com.jcabi.urn.URN;
import com.rultor.spi.Column;
import com.rultor.spi.Pageable;
import com.rultor.spi.Sheet;
import com.rultor.tools.Time;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

/**
 * Sheet in PostgreSQL.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "client", "owner", "orders", "groups" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
final class PgSheet implements Sheet {

    /**
     * Mapping in case of aggregate.
     */
    private static final ImmutableMap<String, String> AGGREGATE =
        new ImmutableMap.Builder<String, String>()
            // @checkstyle MultipleStringLiterals (8 lines)
            .put("id", "MAX(id)")
            .put("time", "MAX(time)")
            .put("ct", "MAX(ct)")
            .put("ctunit", "MAX(ctunit)")
            .put("dt", "MAX(dt)")
            .put("dtunit", "MAX(dtunit)")
            .put("details", "MAX(details)")
            .put("amount", "SUM(amount)")
            .build();

    /**
     * All columns.
     */
    private static final List<Column> COLS = Arrays.<Column>asList(
        new Column.Simple("id", false),
        new Column.Simple("time", false),
        new Column.Simple("ct", true),
        new Column.Simple("ctunit", true),
        new Column.Simple("dt", true),
        new Column.Simple("dtunit", true),
        new Column.Simple("details", false),
        new Column.Simple("amount", false)
    );

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
    private final transient ArrayMap<String, Boolean> orders;

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
        this(
            clnt, urn,
            new ArrayMap<String, Boolean>(), new ArrayList<String>(0)
        );
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
        final Map<String, Boolean> ords, final Collection<String> grps) {
        this.client = clnt;
        this.owner = urn;
        this.orders = new ArrayMap<String, Boolean>(ords);
        this.groups = new ArraySortedSet<String>(
            grps, new ArraySortedSet.Comparator.Default<String>()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Column> columns() {
        return PgSheet.COLS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet orderBy(final String column, final boolean asc) {
        return new PgSheet(
            this.client, this.owner, this.orders.with(column, asc), this.groups
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
    public String toString() {
        return this.query();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<List<Object>> iterator() {
        try {
            return new JdbcSession(this.client.get())
                .sql(this.query())
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
        } catch (SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet between(final Time left, final Time right) {
        return this;
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

    /**
     * Make a query.
     * @return SQL query
     */
    public String query() {
        return new StringBuilder()
            .append("SELECT ")
            .append(StringUtils.join(this.select(), ","))
            .append(" FROM receipt")
            .append(" WHERE ct = ? OR dt = ?")
            .append(this.groupBy())
            .append(this.orderBy())
            .append(" LIMIT 50")
            .toString();
    }

    /**
     * Get names to select from SQL.
     * @return List of names
     */
    private Collection<String> select() {
        final Collection<String> names = new LinkedList<String>();
        for (Column col : this.columns()) {
            if (PgSheet.AGGREGATE.containsKey(col.title())
                && !this.groups.contains(col.title())) {
                names.add(
                    String.format(
                        "%s AS _%s",
                        PgSheet.AGGREGATE.get(col.title()),
                        col.title()
                    )
                );
            } else {
                names.add(col.title());
            }
        }
        return names;
    }

    /**
     * Get GROUP BY statement.
     * @return Statement
     */
    private String groupBy() {
        final StringBuilder stmt = new StringBuilder();
        if (!this.groups.isEmpty()) {
            stmt.append(" GROUP BY ")
                .append(StringUtils.join(this.groups, ','));
        }
        return stmt.toString();
    }

    /**
     * Get ORDER BY statement.
     * @return Statement
     */
    private String orderBy() {
        final StringBuilder stmt = new StringBuilder();
        if (!this.orders.isEmpty()) {
            stmt.append(" ORDER BY ");
            boolean first = true;
            for (Map.Entry<String, Boolean> order : this.orders.entrySet()) {
                if (!first) {
                    stmt.append(',');
                }
                first = false;
                if (!this.groups.contains(order.getKey())) {
                    stmt.append('_');
                }
                stmt.append(order.getKey()).append(' ');
                if (order.getValue()) {
                    stmt.append("ASC");
                } else {
                    stmt.append("DESC");
                }
            }
        }
        return stmt.toString();
    }

}
