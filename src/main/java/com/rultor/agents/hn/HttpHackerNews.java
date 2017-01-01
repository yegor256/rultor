/**
 * Copyright (c) 2009-2017, rultor.com
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
package com.rultor.agents.hn;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsoupResponse;
import com.jcabi.http.response.RestResponse;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Hacker News Update.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.58
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "login", "password" })
public final class HttpHackerNews implements HackerNews {

    /**
     * Login.
     */
    private final transient String login;

    /**
     * Password.
     */
    private final transient String password;

    /**
     * Ctor.
     * @param user Login
     * @param pwd Password
     */
    public HttpHackerNews(final String user, final String pwd) {
        this.login = user;
        this.password = pwd;
    }

    @Override
    public void post(final String url, final String text) throws IOException {
        final String cookie = String.format(
            "user=%s",
            new JdkRequest("https://news.ycombinator.com/login")
                .body()
                .formParam("acct", this.login)
                .formParam("pw", this.password)
                .back()
                .method(Request.POST)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_MOVED_TEMP)
                .cookie("user").getValue()
        );
        final XML submit = new XMLDocument(
            new JdkRequest("https://news.ycombinator.com/submit")
                .header(HttpHeaders.COOKIE, cookie)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(JsoupResponse.class).body()
                .replace(
                    "<html ",
                    "<html xmlns='http://www.w3.org/1999/xhtml' "
                )
        );
        new JdkRequest("https://news.ycombinator.com/")
            .uri()
            .path(submit.xpath("//xhtml:form/@action").get(0))
            .back()
            .header(HttpHeaders.COOKIE, cookie)
            .body()
            .formParam(
                "fnid",
                submit.xpath("//xhtml:input[@name='fnid']/@value").get(0)
            )
            .formParam("fnop", "submit-page")
            .formParam("title", text)
            .formParam("url", url)
            .formParam("text", "")
            .back()
            .method(Request.POST)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_MOVED_TEMP)
            .assertHeader(HttpHeaders.LOCATION, "newest");
    }

}
