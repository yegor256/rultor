/**
 * Copyright (c) 2009-2015, rultor.com
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
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * First comment in any home.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.50.6
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "home")
final class FirstComment implements Comment {

    /**
     * Home issue.
     */
    private final transient Issue.Smart home;

    /**
     * Ctor.
     * @param issue Home issue
     */
    FirstComment(final Issue.Smart issue) {
        this.home = issue;
    }

    @Override
    public Issue issue() {
        return this.home;
    }

    @Override
    public int number() {
        return 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("#remove()");
    }

    @Override
    public int compareTo(final Comment comment) {
        return 1;
    }

    @Override
    public void patch(final JsonObject json) {
        throw new UnsupportedOperationException("#patch()");
    }

    @Override
    public JsonObject json() throws IOException {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        json.add(
            "user",
            Json.createObjectBuilder().add(
                "login", this.home.author().login()
            )
        );
        final String body;
        if (this.home.hasBody()) {
            body = this.home.body();
        } else {
            body = "";
        }
        json.add("body", body);
        return json.build();
    }
}

