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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Loggable;
import com.rultor.tools.Vext;
import java.io.IOException;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;

/**
 * Widget.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Path("/widget")
@Loggable(Loggable.DEBUG)
public final class WidgetRs extends BaseRs {

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     * @throws IOException If fails
     */
    @GET
    @Path("/all.xsl")
    @Produces("text/xsl")
    public String stylesheet() throws IOException {
        final Vext vext = new Vext(
            IOUtils.toString(
                this.getClass().getResourceAsStream("widget.xsl.vm"),
                CharEncoding.UTF_8
            )
        );
        return vext.print(
            new ImmutableMap.Builder<String, Object>()
                .put("stylesheets", WidgetRs.stylesheets())
                .build()
        );
    }

    /**
     * Find and collect all stylesheets.
     * @return Map of classes and their stylesheets
     * @throws IOException If fails
     */
    @Cacheable(forever = true)
    private static Map<String, String> stylesheets() {
        final ImmutableMap.Builder<String, String> stylesheets =
            new ImmutableMap.Builder<String, String>();
        stylesheets.put("com.rultor.widget.Alpha", "");
        return stylesheets.build();
    }

}
