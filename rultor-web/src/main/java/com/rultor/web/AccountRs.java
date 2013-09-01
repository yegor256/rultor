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
package com.rultor.web;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Column;
import com.rultor.spi.Sheet;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Account of a user.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/account")
@Loggable(Loggable.DEBUG)
public final class AccountRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_SINCE = "since";

    /**
     * Start.
     */
    public static final String QUERY_START = "start";

    /**
     * Finish.
     */
    public static final String QUERY_FINISH = "finish";

    /**
     * ASC sort by.
     */
    public static final String QUERY_ASC = "asc";

    /**
     * DESC sort by.
     */
    public static final String QUERY_DESC = "desc";

    /**
     * Group by.
     */
    public static final String QUERY_GROUP = "group";

    /**
     * Since (position).
     */
    private transient int since;

    /**
     * Start.
     */
    private transient Time start = new Time(
        new Date().getTime() - TimeUnit.DAYS.toMillis(Tv.THIRTY)
    );

    /**
     * Finish.
     */
    private transient Time finish = new Time();

    /**
     * Sort in ASC order.
     */
    private final transient Set<String> asc = new HashSet<String>(0);

    /**
     * Sort in DESC order.
     */
    private final transient Set<String> desc = new HashSet<String>(0);

    /**
     * Columns to group by.
     */
    private final transient Set<String> groups = new HashSet<String>(0);

    /**
     * Inject it from query.
     * @param pos Since what position
     */
    @QueryParam(AccountRs.QUERY_SINCE)
    public void setSince(final String pos) {
        if (pos != null) {
            this.since = Integer.valueOf(pos);
        }
    }

    /**
     * Inject it from query.
     * @param txt Interval
     */
    @QueryParam(AccountRs.QUERY_START)
    public void setStart(final String txt) {
        if (txt != null) {
            this.start = new Time(txt);
        }
    }

    /**
     * Inject it from query.
     * @param txt Interval
     */
    @QueryParam(AccountRs.QUERY_FINISH)
    public void setFinish(final String txt) {
        if (txt != null) {
            this.finish = new Time(txt);
        }
    }

    /**
     * Inject it from query.
     * @param columns Sorts
     */
    @QueryParam(AccountRs.QUERY_ASC)
    public void setAsc(final List<String> columns) {
        if (columns != null) {
            this.asc.addAll(columns);
        }
    }

    /**
     * Inject it from query.
     * @param columns Sorts
     */
    @QueryParam(AccountRs.QUERY_DESC)
    public void setDesc(final List<String> columns) {
        if (columns != null) {
            this.desc.addAll(columns);
        }
    }

    /**
     * Inject it from query.
     * @param columns Sorts
     */
    @QueryParam(AccountRs.QUERY_GROUP)
    public void setGroup(final List<String> columns) {
        if (columns != null) {
            this.groups.addAll(columns);
        }
    }

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     * @throws IOException If fails on tailing
     */
    @GET
    @Path("/")
    public Response index() throws IOException {
        final Sheet sheet = this.sheet();
        return new PageBuilder()
            .stylesheet("/xsl/account.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new Breadcrumbs().with("account").bundle())
            .append(new JaxbBundle("sql", sheet.toString()))
            .append(new JaxbBundle("since", Integer.toString(this.since)))
            .append(
                new JaxbBundle("columns").add(
                    new JaxbBundle.Group<Column>(sheet.columns()) {
                        @Override
                        public JaxbBundle bundle(final Column column) {
                            return AccountRs.this.column(column);
                        }
                    }
                )
            )
            .link(
                new Link(
                    "latest",
                    this.home().replaceQueryParam(AccountRs.QUERY_SINCE, 0)
                )
            )
            .append(this.receipts(sheet.tail(this.since).iterator(), Tv.TWENTY))
            .render()
            .build();
    }

    /**
     * Show receipts.
     * @param receipts Receipts
     * @param maximum Maximum to show
     * @return Collection of JAXB rules
     */
    private JaxbBundle receipts(final Iterator<List<Object>> receipts,
        final int maximum) {
        JaxbBundle bundle = new JaxbBundle("receipts");
        int pos;
        for (pos = 0; pos < maximum; ++pos) {
            if (!receipts.hasNext()) {
                break;
            }
            bundle = bundle.add(this.receipt(receipts.next()));
        }
        if (pos == maximum && receipts.hasNext()) {
            bundle = bundle.link(
                new Link(
                    "more",
                    this.home().replaceQueryParam(
                        AccountRs.QUERY_SINCE, this.since + pos
                    )
                )
            );
        }
        return bundle;
    }

    /**
     * Receipt to bundle.
     * @param receipt Receipt
     * @return Bundle
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private JaxbBundle receipt(final List<Object> receipt) {
        JaxbBundle bundle = new JaxbBundle("receipt");
        for (int idx = 0; idx < receipt.size(); ++idx) {
            String cell = receipt.get(idx).toString();
            if (idx == receipt.size() - 1) {
                cell = new Dollars(Long.parseLong(cell)).toString();
            }
            bundle = bundle.add("cell", cell).up();
        }
        return bundle;
    }

    /**
     * Column to bundle.
     * @param column Column
     * @return Bundle
     */
    private JaxbBundle column(final Column column) {
        JaxbBundle bundle = new JaxbBundle("column")
            .add("title", column.title())
            .up();
        if (this.asc.contains(column.title())) {
            bundle = bundle.attr("sorted", "asc");
        }
        if (this.desc.contains(column.title())) {
            bundle = bundle.attr("sorted", "desc");
        }
        if (this.groups.contains(column.title())) {
            bundle = bundle.attr("grouped", "yes");
        }
        if (column.isSum()) {
            bundle = bundle.attr("sum", "yes");
        }
        if (column.isGroup()) {
            bundle.link(
                new Link(
                    "group",
                    this.home().queryParam(
                        AccountRs.QUERY_GROUP, column.title()
                    )
                )
            );
        }
        bundle.link(
            new Link(
                "asc",
                this.home().queryParam(AccountRs.QUERY_ASC, column.title())
            )
        );
        bundle.link(
            new Link(
                "desc",
                this.home().queryParam(AccountRs.QUERY_DESC, column.title())
            )
        );
        return bundle;
    }

    /**
     * Create URI builder with currently set params.
     * @return URI builder
     */
    private UriBuilder home() {
        final UriBuilder builder = this.uriInfo()
            .getBaseUriBuilder()
            .clone()
            .path(AccountRs.class)
            .queryParam(AccountRs.QUERY_SINCE, this.since)
            .queryParam(AccountRs.QUERY_START, this.start)
            .queryParam(AccountRs.QUERY_FINISH, this.finish);
        for (String grp : this.groups) {
            builder.queryParam(AccountRs.QUERY_GROUP, grp);
        }
        for (String col : this.asc) {
            builder.queryParam(AccountRs.QUERY_ASC, col);
        }
        for (String col : this.desc) {
            builder.queryParam(AccountRs.QUERY_DESC, col);
        }
        return builder;
    }

    /**
     * Fetch sheet.
     * @return Sheet
     */
    private Sheet sheet() {
        Sheet sheet = this.user().account().sheet()
            .between(this.start, this.finish);
        for (String grp : this.groups) {
            sheet = sheet.groupBy(grp);
        }
        for (String col : this.asc) {
            sheet = sheet.orderBy(col, true);
        }
        for (String col : this.desc) {
            sheet = sheet.orderBy(col, false);
        }
        return sheet;
    }

}
