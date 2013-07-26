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
import com.rultor.spi.Statement;
import com.rultor.spi.Statements;
import com.rultor.tools.Time;
import java.util.Iterator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Finances of a user.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/finances")
@Loggable(Loggable.DEBUG)
public final class FinancesRs extends BaseRs {

    /**
     * Query param.
     */
    public static final String QUERY_SINCE = "since";

    /**
     * Since (date).
     */
    private transient Time since;

    /**
     * Inject it from query.
     * @param time Since what time
     */
    @QueryParam(FinancesRs.QUERY_SINCE)
    public void setSince(final String time) {
        if (time != null) {
            this.since = new Time(Long.parseLong(time));
        }
    }

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        EmptyPage page = new PageBuilder()
            .stylesheet("/xsl/finances.xsl")
            .build(EmptyPage.class)
            .init(this)
            .link(
                new Link(
                    "receipts",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(ReceiptsRs.class)
                        .build()
                )
            );
        Statements statements = this.user().statements();
        final int total;
        if (this.since == null) {
            total = Tv.FIVE;
        } else {
            statements = statements.tail(this.since);
            total = Tv.TWENTY;
            page = page
                .append(new JaxbBundle("since", this.since.toString()))
                .link(
                    new Link(
                        "latest",
                        this.uriInfo().getBaseUriBuilder()
                            .clone()
                            .path(FinancesRs.class)
                    )
                );
        }
        return page
            .append(this.statements(statements.iterator(), total))
            .render()
            .build();
    }

    /**
     * All statements of the user.
     * @param statements All statements to show
     * @param maximum Maximum to show
     * @return Collection of JAXB units
     */
    private JaxbBundle statements(final Iterator<Statement> statements,
        final int maximum) {
        JaxbBundle bundle = new JaxbBundle("statements");
        int pos;
        for (pos = 0; pos < maximum; ++pos) {
            if (!statements.hasNext()) {
                break;
            }
            bundle = bundle.add(this.statement(statements.next()));
        }
        if (pos == maximum && statements.hasNext()) {
            bundle = bundle.link(
                new Link(
                    "more",
                    this.uriInfo()
                        .getBaseUriBuilder()
                        .clone()
                        .path(DrainRs.class)
                        .queryParam(
                            FinancesRs.QUERY_SINCE,
                            statements.next().date().millis()
                        )
                        .build()
                )
            );
        }
        return bundle;
    }

    /**
     * Convert statement to JaxbBundle.
     * @param statement Statement to render
     * @return Bundle
     */
    private JaxbBundle statement(final Statement statement) {
        return new JaxbBundle("statement")
            .add("amount", statement.amount().toString())
            .up()
            .add("balance", statement.balance().toString())
            .up()
            .add("date", statement.date().toString())
            .up()
            .add(
                "when",
                DurationFormatUtils.formatDurationWords(
                    System.currentTimeMillis() - statement.date().millis(),
                    true, true
                )
            )
            .up();
    }

}
