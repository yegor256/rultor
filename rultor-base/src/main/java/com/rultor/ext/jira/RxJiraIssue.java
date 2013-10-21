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
package com.rultor.ext.jira;

import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rexsl.test.RestTester;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.http.HttpHeaders;

/**
 * Jira issue with ReXSL.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @see <a href="https://docs.atlassian.com/jira/REST/latest/">JIRA REST API</a>
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "url")
@Loggable(Loggable.DEBUG)
final class RxJiraIssue implements JiraIssue {

    /**
     * URL of the server.
     */
    private final transient String url;

    /**
     * Public ctor.
     * @param srv Server URL
     */
    protected RxJiraIssue(final URI srv) {
        this.url = srv.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String key() {
        final URI uri = UriBuilder.fromUri(this.url)
            .queryParam("fields", "")
            .queryParam("expand", "")
            .build();
        return RestTester.start(uri)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .get("fetching issue key")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .getJson()
            .readObject()
            .getString("key");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assign(final String name) {
        final URI uri = UriBuilder.fromUri(this.url)
            .path("/assignee")
            .build();
        final StringWriter json = new StringWriter();
        Json.createGenerator(json)
            .writeStartObject()
            .write("name", name)
            .writeEnd()
            .close();
        RestTester.start(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .put("assigning issue", json.toString())
            .assertStatus(HttpURLConnection.HTTP_NO_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void revert(final String message) {
        final Iterable<JiraComment> comments = this.comments();
        if (Iterables.isEmpty(comments)) {
            this.assign("");
        } else {
            this.assign(comments.iterator().next().author());
        }
        if (!message.isEmpty()) {
            this.post(message);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @todo #307 Paging is not implemented
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterable<JiraComment> comments() {
        final URI uri = UriBuilder.fromUri(this.url)
            // @checkstyle MultipleStringLiterals (1 line)
            .path("/comment")
            .build();
        final JsonArray json = RestTester.start(uri)
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
            .get("fetch list of comments of an issue")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .getJson()
            .readObject()
            .getJsonArray("comments");
        final List<JiraComment> lst =
            new ArrayList<JiraComment>(json.size());
        for (JsonValue obj : json) {
            lst.add(
                new RxJiraComment(
                    UriBuilder.fromUri(
                        JsonObject.class.cast(obj).getString("self")
                    ).userInfo(uri.getUserInfo()).build()
                )
            );
        }
        Collections.reverse(lst);
        return lst;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void post(final String body) {
        final URI uri = UriBuilder.fromUri(this.url)
            .path("/comment")
            .build();
        final StringWriter json = new StringWriter();
        Json.createGenerator(json)
            .writeStartObject()
            .write("body", body)
            .writeEnd()
            .close();
        RestTester.start(uri)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .post("posting comment", json.toString())
            .assertStatus(HttpURLConnection.HTTP_CREATED);
    }

}
