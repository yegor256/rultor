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
package com.rultor.ext.jira;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.http.HttpHeaders;

/**
 * Jira with ReXSL.
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
public final class RxJira implements Jira {

    /**
     * URL of the server.
     */
    private final transient String url;

    /**
     * Public ctor.
     * @param srv Server URL
     */
    public RxJira(final String srv) {
        this.url = srv;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterable<JiraIssue> search(final String jql) {
        final URI uri = UriBuilder.fromUri(this.url)
            .path("/search")
            .queryParam("jql", "{jql}")
            .queryParam("fields", "")
            .queryParam("expand", "")
            .build(jql);
        final JsonArray json;
        try {
            json = new JdkRequest(uri)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(JsonResponse.class)
                .json()
                .readObject()
                .getJsonArray("issues");
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        final Collection<JiraIssue> lst = new ArrayList<JiraIssue>(json.size());
        for (final JsonValue obj : json) {
            lst.add(
                new RxJiraIssue(
                    UriBuilder.fromUri(
                        JsonObject.class.cast(obj).getString("self")
                    ).userInfo(uri.getUserInfo()).build()
                )
            );
        }
        return lst;
    }

}
