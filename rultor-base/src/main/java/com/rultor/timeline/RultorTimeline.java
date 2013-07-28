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
package com.rultor.timeline;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.rexsl.test.RestTester;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;

/**
 * Timeline in {@code http://timeline.rultor.com}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "name", "key" })
@Loggable(Loggable.DEBUG)
public final class RultorTimeline implements Timeline {

    /**
     * Name of timeline.
     */
    private final transient String name;

    /**
     * Authentication key.
     */
    private final transient String key;

    /**
     * Public ctor.
     * @param label Name of timeline
     * @param secret Secret authentication key
     */
    public RultorTimeline(
        @NotNull(message = "name can't be NULL") final String label,
        @NotNull(message = "key can't be NULL") final String secret) {
        this.name = label;
        this.key = secret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "timeline `%s` in `timeline.rultor.com`",
            this.name
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RetryOnFailure
    public void submit(final String text, final Collection<Tag> tags,
        final Collection<Product> products) throws IOException {
        try {
            this.post(
                UriBuilder.fromUri("http://timeline.rultor.com/t/")
                    .path(this.name)
                    .path("/post")
                    .build(),
                this.json(text, tags, products)
            );
        } catch (AssertionError ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Post it.
     * @param uri Destination URI
     * @param json JSON to post
     */
    @RetryOnFailure
    private void post(final URI uri, final String json) {
        RestTester.start(uri)
            .header("X-Rultor-Key", this.key)
            .header(
                HttpHeaders.USER_AGENT,
                String.format(
                    "%s %s/%s",
                    this.getClass().getName(),
                    Manifests.read("Rultor-Version"),
                    Manifests.read("Rultor-Revision")
                )
            )
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .post("posting event", json)
            .assertStatus(HttpURLConnection.HTTP_CREATED);
    }

    /**
     * Make JSON.
     * @param text Text
     * @param tags Tags
     * @param products Products
     * @return JSON to be posted
     */
    private String json(final String text, final Collection<Tag> tags,
        final Collection<Product> products) {
        final StringWriter output = new StringWriter();
        final JsonGenerator json = Json.createGenerator(output);
        json.writeStartObject().write("text", text).writeStartArray("tags");
        for (Tag tag : tags) {
            json.writeStartObject()
                .write("label", tag.label())
                .write("level", tag.level().toString())
                .writeEnd();
        }
        json.writeEnd().writeStartArray("products");
        for (Product product : products) {
            json.writeStartObject()
                .write("name", product.name())
                .write("markdown", product.markdown())
                .writeEnd();
        }
        json.writeEnd().writeEnd().close();
        return output.toString();
    }

}
