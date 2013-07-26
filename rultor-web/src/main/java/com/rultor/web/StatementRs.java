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
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.PageBuilder;
import com.rultor.spi.Statement;
import com.rultor.tools.Time;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
@Path("/statement/{time:\\d+}")
@Loggable(Loggable.DEBUG)
public final class StatementRs extends BaseRs {

    /**
     * Exact time of the statement.
     */
    private transient Time time;

    /**
     * Inject it from path.
     * @param txt Text of the value
     */
    @PathParam("time")
    public void setSince(@NotNull(message = "time path param is mandatory")
        final String txt) {
        this.time = new Time(Long.parseLong(txt));
    }

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/statement.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(this.statement(this.user().statements().get(this.time)))
            .render()
            .build();
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
            .add("details", statement.details())
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
