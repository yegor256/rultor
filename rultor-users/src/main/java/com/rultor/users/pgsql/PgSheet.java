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
import com.jcabi.aspects.Tv;
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
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
     * Comma.
     */
    private static final String COMMA = ", ";

    /**
     * All columns.
     */
    private static final List<Column> COLS = Arrays.<Column>asList(
        new Column.Simple("id", false, false),
        new Column.Simple("time", false, false),
        new Column.Simple("ct", true, false),
        new Column.Simple("ctrule", true, false),
        new Column.Simple("dt", true, false),
        new Column.Simple("dtrule", true, false),
        new Column.Simple("details", false, false),
        new Column.Simple("amount", false, true)
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
     * Since which position to start.
     */
    private final transient int since;

    /**
     * Start.
     */
    private final transient Time start;

    /**
     * End.
     */
    private final transient Time end;

    /**
     * Order by.
     */
    private final transient ArrayMap<String, Boolean> orders;

    /**
     * Group by.
     */
    private final transient ArraySortedSet<String> groups;

    /**
     * Extra WHERE clause.
     */
    private final transient String clause;

    /**
     * Public ctor.
     * @param clnt Client
     * @param urn URN of the owner
     */
    protected PgSheet(final PgClient clnt, final URN urn) {
        this(
            clnt, urn,
            new ArrayMap<String, Boolean>(), new ArrayList<String>(0),
            new Time(new Date().getTime() - TimeUnit.DAYS.toMillis(Tv.SEVEN)),
            new Time(),
            0, ""
        );
    }

    /**
     * Public ctor.
     * @param clnt Client
     * @param urn URN of the owner
     * @param ords Orders
     * @param grps Groups
     * @param left Start of interval
     * @param right End of interval
     * @param first Which line to show first
     * @param where Extra WHERE clause
     * @checkstyle ParameterNumber (5 lines)
     */
    protected PgSheet(final PgClient clnt, final URN urn,
        final Map<String, Boolean> ords, final Collection<String> grps,
        final Time left, final Time right, final int first,
        final String where) {
        this.client = clnt;
        this.owner = urn;
        this.orders = new ArrayMap<String, Boolean>(ords);
        this.groups = new ArraySortedSet<String>(
            grps, new ArraySortedSet.Comparator.Default<String>()
        );
        this.clause = where;
        this.start = left;
        this.end = right;
        this.since = first;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Column> columns() {
        return Collections.unmodifiableList(PgSheet.COLS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet orderBy(final String column, final boolean asc) {
        return new PgSheet(
            this.client, this.owner, this.orders.with(column, asc), this.groups,
            this.start, this.end, this.since, this.clause
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet groupBy(final String column) {
        return new PgSheet(
            this.client, this.owner, this.orders, this.groups.with(column),
            this.start, this.end, this.since, this.clause
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<List<Object>, Integer> tail(final Integer head)
        throws IOException {
        return new PgSheet(
            this.client, this.owner, this.orders, this.groups,
            this.start, this.end, head, this.clause
        );
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
        final String sql = this.query();
        try {
            return new JdbcSession(this.client.get())
                .sql(sql)
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
            throw new IllegalStateException(sql, ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet between(final Time left, final Time right) {
        return new PgSheet(
            this.client, this.owner, this.orders, this.groups,
            left, right, this.since, this.clause
        );
    }

    /**
     * With this extra WHERE clause.
     * @param where WHERE clause
     * @return New sheet
     */
    public Sheet with(final String where) {
        return new PgSheet(
            this.client, this.owner, this.orders, this.groups,
            this.start, this.end, this.since, where
        );
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
            .append(this.select())
            .append("\nFROM receipt")
            .append('\n').append(this.whereOn())
            .append(this.groupBy())
            .append(this.orderBy())
            .append('\n').append(this.limit())
            .toString();
    }

    @Override
    public Sheet.Condition where() {
        return new PgCondition(this);
    }

    /**
     * Get names to select from SQL.
     * @return List of names
     */
    private String select() {
        final Collection<String> names = new LinkedList<String>();
        for (Column col : this.columns()) {
            if (this.groups.isEmpty() || this.groups.contains(col.title())) {
                names.add(col.title());
            } else {
                final String func;
                if (col.isSum()) {
                    func = "SUM";
                } else {
                    func = "MAX";
                }
                names.add(
                    String.format(
                        "%s(%s) AS %s",
                        func,
                        col.title(),
                        this.ref(col.title())
                    )
                );
            }
        }
        return StringUtils.join(names, PgSheet.COMMA);
    }

    /**
     * Get WHERE statement.
     * @return Statement
     */
    private String whereOn() {
        final StringBuilder sql = new StringBuilder()
            .append("WHERE (ct='")
            .append(this.owner)
            .append("' OR dt='")
            .append(this.owner)
            .append("') AND time >= '")
            .append(this.start)
            .append("' AND time <= '")
            .append(this.end)
            .append("'");
        if (!this.clause.isEmpty()) {
            sql.append(" AND ").append(this.clause);
        }
        return sql.toString();
    }

    /**
     * Get GROUP BY statement.
     * @return Statement
     */
    private String groupBy() {
        final StringBuilder stmt = new StringBuilder();
        if (!this.groups.isEmpty()) {
            stmt.append("\nGROUP BY ")
                .append(StringUtils.join(this.groups, PgSheet.COMMA));
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
            stmt.append("\nORDER BY ");
            boolean first = true;
            for (Map.Entry<String, Boolean> order : this.orders.entrySet()) {
                if (!first) {
                    stmt.append(PgSheet.COMMA);
                }
                first = false;
                stmt.append(this.ref(order.getKey())).append(' ');
                if (order.getValue()) {
                    stmt.append("ASC");
                } else {
                    stmt.append("DESC");
                }
            }
        }
        return stmt.toString();
    }

    /**
     * Get LIMIT statement.
     * @return Statement
     */
    private String limit() {
        return new StringBuilder()
            .append("LIMIT 50 OFFSET ")
            .append(this.since)
            .toString();
    }

    /**
     * Reference name of a column.
     * @param column Column name
     * @return Ref name
     */
    private String ref(final String column) {
        final String ref;
        if (this.groups.isEmpty() || this.groups.contains(column)) {
            ref = column;
        } else {
            ref = String.format("_%s", column);
        }
        return ref;
    }

}
