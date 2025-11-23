/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Coordinates;
import com.jcabi.github.GitHub;
import com.jcabi.github.Issue;
import com.jcabi.github.RtPagination;
import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import com.jcabi.log.Logger;
import com.rultor.Time;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import jakarta.json.JsonObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directives;

/**
 * Starts talk when I'm mentioned in a GitHub issue.
 *
 * @since 1.0
 * @todo #1074:1h Current implementation can answer only for
 *  issue and PR comments, mostly because of Issue structure
 *  from jcabi-github (id is integer). For now mentions NOT
 *  from issue or PR will be ignored. Can be extended if
 *  more generic entity will be created to cover commits
 *  and may be other types.
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "github")
public final class StartsTalks implements SuperAgent {

    /**
     * GitHub.
     */
    private final transient GitHub github;

    /**
     * Ctor.
     * @param ghub GitHub client
     */
    public StartsTalks(final GitHub ghub) {
        this.github = ghub;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void execute(final Talks talks) throws IOException {
        final String since = new Time(
            System.currentTimeMillis() - 180_000
        ).iso();
        final Request req = this.github.entry()
            .uri().path("/notifications").back();
        final Iterable<JsonObject> events = new RtPagination<>(
            req.uri()
                .queryParam("participating", "true")
                .queryParam("since", since)
                .queryParam("all", Boolean.toString(true))
                .back(),
            RtPagination.COPYING
        );
        final Collection<String> names = new LinkedList<>();
        int skipped = 0;
        for (final JsonObject event : events) {
            final String url = event.getJsonObject("subject").getString("url");
            final String reason = event.getString("reason");
            if (!"mention".equals(reason)) {
                ++skipped;
                Logger.info(
                    this, "Skipped, since not interesting reason '%s' in %s",
                    reason, url
                );
                continue;
            }
            if (!new IssueUrl(url).valid()) {
                ++skipped;
                Logger.info(this, "Skipped, since not valid URL at %s", url);
                continue;
            }
            names.add(this.activate(talks, event));
        }
        req.uri()
            .queryParam("last_read_at", since).back()
            .method(Request.PUT)
            .body().set("{}").back()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_RESET);
        Logger.info(
            this, "%d new notification(s) since %s (%d skipped): %[list]s",
            names.size(), since, skipped, names
        );
    }

    /**
     * Activate talk.
     * @param talks Talks
     * @param event Event
     * @return Name of the talk activated
     * @throws IOException If fails
     */
    private String activate(final Talks talks, final JsonObject event)
        throws IOException {
        final Coordinates coords = this.coords(event);
        final Issue issue = this.github.repos().get(coords).issues().get(
            new IssueUrl(event.getJsonObject("subject").getString("url")).uid()
        );
        final String name = String.format("%s#%d", coords, issue.number());
        if (!talks.exists(name)) {
            talks.create(coords.toString(), name);
        }
        final Talk talk = talks.get(name);
        talk.modify(
            new Directives()
                .xpath("/talk").attr("later", Boolean.toString(true))
                .xpath("/talk[not(wire)]")
                .add("wire").add("href")
                .set(new Issue.Smart(issue).htmlUrl().toString())
                .up()
                .add("github-repo").set(coords.toString())
                .up()
                .add("github-issue")
                .set(Integer.toString(issue.number()))
        );
        talk.active(true);
        Logger.info(
            this, "talk %s#%d activated as %s",
            coords, issue.number(), name
        );
        return talk.name();
    }

    /**
     * Get coordinates from JSON.
     * @param event Event
     * @return Coords
     * @checkstyle NonStaticMethodCheck (5 lines)
     */
    private Coordinates coords(final JsonObject event) {
        return new Coordinates.Simple(
            event.getJsonObject("repository").getString("full_name")
        );
    }

}
