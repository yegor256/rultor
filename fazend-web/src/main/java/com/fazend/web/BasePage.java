/**
 * Copyright (c) 2009-2013, fazend.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the fazend.com nor
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
package com.fazend.web;

import com.jcabi.aspects.Loggable;
import com.jcabi.manifests.Manifests;
import com.rexsl.page.JaxbBundle;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base RESTful page.
 *
 * <p>All other JAXB pages are inherited from this class, in runtime,
 * by means of {@link com.rexsl.page.PageBuilder}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 2.0
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
public class BasePage extends com.rexsl.page.BasePage<BasePage, BaseRs> {

    /**
     * Render it.
     * @return JAX-RS response
     */
    @Loggable(Loggable.DEBUG)
    public final Response.ResponseBuilder render() {
        final Response.ResponseBuilder builder = Response.ok();
        this.append(
            new JaxbBundle("version")
                .add("name", Manifests.read("Fazend-Version"))
                .up()
                .add("revision", Manifests.read("Fazend-Revision"))
                .up()
                .add("date", Manifests.read("Fazend-Date"))
                .up()
        );
        builder.entity(this);
        builder.type(MediaType.TEXT_XML);
        builder.header(HttpHeaders.VARY, "Cookie");
        return builder;
    }

}
