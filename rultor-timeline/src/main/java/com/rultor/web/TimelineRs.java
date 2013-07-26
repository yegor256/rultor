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
import com.rultor.timeline.Event;
import com.rultor.timeline.Product;
import com.rultor.timeline.Tag;
import com.rultor.timeline.Timeline;
import com.rultor.timeline.Timelines;
import com.rultor.tools.Markdown;
import com.rultor.tools.Time;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Front page of get.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Path("/t/{name:[a-z]+}")
@Loggable(Loggable.DEBUG)
public final class TimelineRs extends BaseRs {

    /**
     * Timeline.
     */
    private transient Timeline timeline;

    /**
     * Inject it from query.
     * @param name Name of get
     */
    @PathParam("name")
    public void setName(@NotNull(message = "unit name can't be NULL")
        final String name) {
        try {
            this.timeline = this.timelines().get(name);
        } catch (Timelines.TimelineNotFoundException ex) {
            throw new WebApplicationException(
                ex, HttpURLConnection.HTTP_NOT_FOUND
            );
        }
    }

    /**
     * Get entrance page JAX-RS response.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response index() {
        return new PageBuilder()
            .stylesheet("/xsl/timeline.xsl")
            .build(EmptyPage.class)
            .init(this)
            .append(new JaxbBundle("name", this.timeline.name()))
            .append(
                new JaxbBundle("events").add(
                    new JaxbBundle.Group<Event>(
                        this.timeline.events(new Time())) {
                        @Override
                        public JaxbBundle bundle(final Event event) {
                            return TimelineRs.this.event(event);
                        }
                    }
                )
            )
            .render()
            .build();
    }

    /**
     * Post into it, in JSON format.
     *
     * <pre>
     * {
     *   "text": "event text in Unicode",
     *   "tags": [
     *     {
     *       "label": "label of a tag, up to 40 characters",
     *       "level": "INFO|WARNING|SEVERE"
     *     }
     *   ],
     *   "products": [
     *     {
     *       "name": "name of a product",
     *     }
     *   ]
     * }
     * </pre>
     *
     * @param body Body of HTTP request
     * @return The JAX-RS response
     */
    @POST
    @Path("/post")
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Response post(final InputStream body) {
        final String hdr = this.httpServletRequest().getHeader("X-Rultor-Key");
        if (hdr == null) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "authentication key expected in X-Rultor-Key HTTP header",
                Level.SEVERE
            );
        }
        if (!hdr.equals(this.timeline.permissions().key())) {
            throw this.flash().redirect(
                this.uriInfo().getBaseUri(),
                "invalid authentication key, access denied",
                Level.SEVERE
            );
        }
        final JsonObject object = Json.createReader(body).readObject();
        final Collection<Tag> tags = new ArrayList<Tag>(0);
        for (JsonValue obj : object.getJsonArray("tags")) {
            final JsonObject json = JsonObject.class.cast(obj);
            tags.add(
                new Tag.Simple(
                    json.getString("label"),
                    Level.parse(json.getString("level"))
                )
            );
        }
        final Event event = this.timeline.post(
            object.getString("text"), tags, new ArrayList<Product>(0)
        );
        throw this.flash().redirect(
            this.uriInfo().getBaseUri(),
            String.format("Event `%s` successfully posted", event.time()),
            Level.INFO
        );
    }

    /**
     * Convert event to JaxbBundle.
     * @param event The event to convert
     * @return Bundle
     */
    private JaxbBundle event(final Event event) {
        return new JaxbBundle("event")
            .add("time", event.time().toString())
            .up()
            .add("when", event.time().when())
            .up()
            .add("html", new Markdown(event.text()).html())
            .up()
            .add("tags")
            .add(
                new JaxbBundle.Group<Tag>(event.tags()) {
                    @Override
                    public JaxbBundle bundle(final Tag tag) {
                        return new JaxbBundle("tag")
                            .add("label", tag.label())
                            .up()
                            .add("level", tag.level().toString())
                            .up();
                    }
                }
            )
            .up()
            .add("products")
            .add(
                new JaxbBundle.Group<Product>(event.products()) {
                    @Override
                    public JaxbBundle bundle(final Product product) {
                        return TimelineRs.this.product(product);
                    }
                }
            )
            .up();
    }

    /**
     * Convert product to JaxbBundle.
     * @param product Product to convert
     * @return Bundle
     */
    private JaxbBundle product(final Product product) {
        return new JaxbBundle("product")
            .add("name", product.name())
            .up()
            .add("html", new Markdown(product.markdown()).html())
            .up();
    }

}
