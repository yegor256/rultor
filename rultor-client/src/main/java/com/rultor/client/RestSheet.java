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
package com.rultor.client;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rexsl.test.RestTester;
import com.rexsl.test.XmlDocument;
import com.rultor.spi.Column;
import com.rultor.spi.Pageable;
import com.rultor.spi.Sheet;
import com.rultor.tools.Time;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * RESTful sheet.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "home", "token" })
@Loggable(Loggable.DEBUG)
final class RestSheet implements Sheet {

    /**
     * Home URI.
     */
    private final transient String home;

    /**
     * Authentication token.
     */
    private final transient String token;

    /**
     * Public ctor, with custom entry point.
     * @param entry Entry point (URI)
     * @param tkn Token
     */
    protected RestSheet(
        @NotNull(message = "URI can't be NULL") final URI entry,
        @NotNull(message = "token can't be NULL") final String tkn) {
        this.home = entry.toString();
        this.token = tkn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<Column> columns() {
        final Collection<XmlDocument> nodes = RestTester
            .start(UriBuilder.fromUri(this.home))
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .header(HttpHeaders.AUTHORIZATION, this.token)
            .get("#balance()")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .nodes("/page/columns/column");
        final List<Column> columns = new ArrayList<Column>(nodes.size());
        for (XmlDocument node : nodes) {
            columns.add(
                new Column.Simple(
                    node.xpath("title/text()").get(0),
                    !node.nodes("links/link[@rel='group']").isEmpty()
                )
            );
        }
        return columns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet orderBy(final String column, final boolean asc) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet groupBy(final String column) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet between(final Time left, final Time right) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<List<Object>, Integer> tail(final Integer head)
        throws IOException {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<List<Object>> iterator() {
        throw new UnsupportedOperationException();
    }

}
