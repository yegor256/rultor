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
import com.rultor.spi.Invoice;
import com.rultor.spi.Invoices;
import com.rultor.spi.Statement;
import com.rultor.spi.Time;
import java.util.Iterator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Statement of a user.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/stmt")
@Loggable(Loggable.DEBUG)
public final class StatementRs extends BaseRs {

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
    @QueryParam(StatementRs.QUERY_SINCE)
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
    @SuppressWarnings("PMD.CloseResource")
    public Response index() {
        final Statement stmt = this.user().statement();
        EmptyPage page = new PageBuilder()
            .stylesheet("/xsl/statement.xsl")
            .build(EmptyPage.class)
            .init(this);
        Invoices invoices = stmt.invoices();
        final int total;
        if (this.since == null) {
            total = Tv.FIVE;
        } else {
            invoices = invoices.tail(this.since);
            total = Tv.TWENTY;
            page = page
                .append(new JaxbBundle("since", this.since.toString()))
                .link(
                    new Link(
                        "latest",
                        this.uriInfo().getBaseUriBuilder()
                            .clone()
                            .path(StatementRs.class)
                    )
                );
        }
        return page
            .append(this.invoices(invoices.iterator(), total))
            .render()
            .build();
    }

    /**
     * All invoices of the statement.
     * @param invoices All invoices to show
     * @param maximum Maximum to show
     * @return Collection of JAXB units
     */
    private JaxbBundle invoices(final Iterator<Invoice> invoices,
        final int maximum) {
        JaxbBundle bundle = new JaxbBundle("invoices");
        int pos;
        for (pos = 0; pos < maximum; ++pos) {
            if (!invoices.hasNext()) {
                break;
            }
            bundle = bundle.add(this.invoice(invoices.next()));
        }
        if (pos == maximum && invoices.hasNext()) {
            bundle = bundle.link(
                new Link(
                    "more",
                    this.uriInfo()
                        .getBaseUriBuilder()
                        .clone()
                        .path(DrainRs.class)
                        .queryParam(
                            StatementRs.QUERY_SINCE,
                            invoices.next().date().millis()
                        )
                        .build()
                )
            );
        }
        return bundle;
    }

    /**
     * Convert invoice to JaxbBundle.
     * @param invoice Invoice to render
     * @return Bundle
     */
    private JaxbBundle invoice(final Invoice invoice) {
        return new JaxbBundle("invoice")
            .add("amount", invoice.amount().toString())
            .up()
            .add("text", invoice.text())
            .up()
            .add("date", invoice.date().toString())
            .up()
            .add(
                "when",
                DurationFormatUtils.formatDurationWords(
                    System.currentTimeMillis() - invoice.date().millis(),
                    true, true
                )
            )
            .up();
    }

}
