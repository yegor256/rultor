/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Widget;
import com.rultor.tools.Vext;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringEscapeUtils;
import org.reflections.Reflections;

/**
 * Stylesheets.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/stylesheets")
@Loggable(Loggable.DEBUG)
public final class StylesheetsRs extends BaseRs {

    /**
     * Type in query.
     */
    private static final String QUERY_TYPE = "type";

    /**
     * Get aggregation XSL.
     * @return The JAX-RS response
     * @throws IOException If fails
     */
    @GET
    @Produces("text/xsl")
    public String aggregation() throws IOException {
        final Vext vext = new Vext(
            IOUtils.toString(
                this.getClass().getResourceAsStream("widget.xsl.vm"),
                CharEncoding.UTF_8
            )
        );
        return vext.print(
            new ImmutableMap.Builder<String, Object>()
                .put("hrefs", this.hrefs())
                .put(
                    "stand",
                    StringEscapeUtils.escapeXml11(
                        this.uriInfo().getBaseUriBuilder()
                            .clone().path("/xsl/stand.xsl")
                            .build().toString()
                    )
                )
                .build()
        );
    }

    /**
     * Get stylesheet of one widget class.
     * @param type Name of class
     * @return XSL stylesheet
     * @throws IOException If fails
     */
    @GET
    @Path("/w.xsl")
    @Produces("text/xsl")
    public String single(
        @QueryParam(StylesheetsRs.QUERY_TYPE) final String type)
        throws IOException {
        return StylesheetsRs.stylesheets().get(type);
    }

    /**
     * Get all HREFs.
     * @return List of HREFs of stylesheets
     * @throws IOException If fails
     */
    private Collection<String> hrefs() throws IOException {
        final Collection<String> hrefs = new LinkedList<String>();
        for (final String type : StylesheetsRs.stylesheets().keySet()) {
            hrefs.add(
                StringEscapeUtils.escapeXml11(
                    this.uriInfo().getBaseUriBuilder()
                        .clone()
                        .path(StylesheetsRs.class)
                        .path(StylesheetsRs.class, "single")
                        .queryParam(StylesheetsRs.QUERY_TYPE, "{t}")
                        .build(type)
                        .toString()
                )
            );
        }
        return hrefs;
    }

    /**
     * Find and collect all stylesheets.
     * @return Map of classes and their stylesheets
     * @throws IOException If fails
     */
    @Cacheable(forever = true)
    private static Map<String, String> stylesheets() throws IOException {
        final ImmutableMap.Builder<String, String> stylesheets =
            new ImmutableMap.Builder<String, String>();
        final Reflections reflections = new Reflections("com.rultor");
        final Collection<Class<?>> types =
            reflections.getTypesAnnotatedWith(Widget.Stylesheet.class);
        for (final Class<?> type : types) {
            stylesheets.put(type.getCanonicalName(), StylesheetsRs.load(type));
        }
        return stylesheets.build();
    }

    /**
     * Load stylesheet for the class.
     * @param type Type to use
     * @return XSL content
     * @throws IOException If fails
     */
    private static String load(final Class<?> type) throws IOException {
        return IOUtils.toString(
            type.getResourceAsStream(
                type.getAnnotation(Widget.Stylesheet.class).value()
            ),
            CharEncoding.UTF_8
        );
    }

}
