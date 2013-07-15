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

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Receipt;
import java.io.StringWriter;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.CharUtils;

/**
 * Receipts of a user.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/receipts")
@Loggable(Loggable.DEBUG)
public final class ReceiptsRs extends BaseRs {

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/receipts.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(
                this.receipts(
                    Iterables.limit(this.user().receipts(), Tv.HUNDRED)
                )
            )
            .link(
                new Link(
                    "text",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(ReceiptsRs.class)
                        .path(ReceiptsRs.class, "text")
                        .build()
                )
            )
            .link(
                new Link(
                    "json",
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(ReceiptsRs.class)
                        .path(ReceiptsRs.class, "json")
                        .build()
                )
            )
            .render()
            .build();
    }

    /**
     * Load them all in plain text.
     * @return The JAX-RS response
     */
    @GET
    @Path("/text")
    @Produces(MediaType.TEXT_PLAIN)
    public String text() {
        final StringBuilder text = new StringBuilder();
        for (Receipt receipt : this.user().receipts()) {
            text.append(receipt.date())
                .append('\t')
                .append(receipt.payer())
                .append('\t')
                .append(receipt.beneficiary())
                .append('\t')
                .append(receipt.unit())
                .append('\t')
                .append(receipt.details())
                .append(CharUtils.LF);
        }
        return text.toString();
    }

    /**
     * Load them all in plain text.
     * @return The JAX-RS response
     */
    @GET
    @Path("/json")
    @Produces(MediaType.TEXT_PLAIN)
    public String json() {
        final JsonArrayBuilder json = Json.createArrayBuilder();
        for (Receipt receipt : this.user().receipts()) {
            json.add(
                Json.createObjectBuilder()
                    .add("date", receipt.date().toString())
                    .add("payer", receipt.payer().toString())
                    .add("beneficiary", receipt.beneficiary().toString())
                    .add("unit", receipt.unit())
                    .add("details", receipt.details())
            );
        }
        final StringWriter writer = new StringWriter();
        Json.createWriter(writer).writeArray(json.build());
        return writer.toString();
    }

    /**
     * All receipts of the user.
     * @param receipts All receipts to show
     * @return Collection of JAXB units
     */
    private JaxbBundle receipts(final Iterable<Receipt> receipts) {
        return new JaxbBundle("receipts").add(
            new JaxbBundle.Group<Receipt>(receipts) {
                @Override
                public JaxbBundle bundle(final Receipt rcpt) {
                    return ReceiptsRs.this.receipt(rcpt);
                }
            }
        );
    }

    /**
     * Convert receipt to JaxbBundle.
     * @param receipt Receipt to render
     * @return Bundle
     */
    private JaxbBundle receipt(final Receipt receipt) {
        return new JaxbBundle("receipt")
            .add("amount", receipt.dollars().toString())
            .up()
            .add("details", receipt.details())
            .up()
            .add("payer", receipt.payer().toString())
            .up()
            .add("beneficiary", receipt.beneficiary().toString())
            .up()
            .add("date", receipt.date().toString())
            .up()
            .add("unit", receipt.unit())
            .up();
    }

}
