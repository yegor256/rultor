/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import com.jcabi.github.Reaction;
import java.io.IOException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * First comment in any home.
 *
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
    public long number() {
        return 1L;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("#remove()");
    }

    @Override
    public void react(final Reaction reaction) throws IOException {
        throw new UnsupportedOperationException("#react()");
    }

    @Override
    public Iterable<Reaction> reactions() {
        throw new UnsupportedOperationException("#reactions()");
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
