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
import com.rultor.spi.Sheet;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

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
     * Since (position).
     */
    private transient Integer since;

    /**
     * Inject it from query.
     * @param pos Since what position
     */
    @QueryParam(AccountRs.QUERY_SINCE)
    public void setSince(final String pos) {
        if (pos == null) {
            this.since = 0;
        } else {
            this.since = Integer.valueOf(pos);
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
        final Sheet sheet = this.user().account().sheet();
        return new PageBuilder()
            .stylesheet("/xsl/account.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new JaxbBundle("since", this.since.toString()))
            .append(
                new JaxbBundle("columns").add(
                    new JaxbBundle.Group<String>(sheet.columns()) {
                        @Override
                        public JaxbBundle bundle(final String title) {
                            return new JaxbBundle("column", title);
                        }
                    }
                )
            )
            .link(
                new Link(
                    "latest",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(AccountRs.class)
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
     * @return Collection of JAXB units
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
                    this.uriInfo()
                        .getBaseUriBuilder()
                        .clone()
                        .path(AccountRs.class)
                        .queryParam(AccountRs.QUERY_SINCE, this.since + pos)
                        .build()
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
    private JaxbBundle receipt(final List<Object> receipt) {
        JaxbBundle bundle = new JaxbBundle("receipt")
            .attr("id", receipt.get(0).toString());
        for (int pos = 1; pos < receipt.size(); ++pos) {
            bundle = bundle.add("cell", receipt.get(pos).toString()).up();
        }
        return bundle;
    }

}
