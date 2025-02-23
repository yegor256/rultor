/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.list.ListOf;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Parametrized question.
 *
 * @since 1.3.6
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class QnParametrized implements Question {

    /**
     * Pattern to find.
     */
    private static final Pattern PTN = Pattern.compile(
        "([a-zA-Z_]+)\\s*(?::|=|is)\\s*`([^`]+)`",
        Pattern.DOTALL | Pattern.MULTILINE
    );

    /**
     * Original question.
     */
    private final transient Question origin;

    /**
     * Ctor.
     * @param qtn Original question
     */
    public QnParametrized(final Question qtn) {
        this.origin = qtn;
    }

    @Override
    public Req understand(final Comment.Smart comment, final URI home)
        throws IOException {
        final Map<String, String> map = QnParametrized.params(comment);
        Req req = this.origin.understand(comment, home);
        final List<Directive> directives = new ListOf<>(req.dirs());
        if (!directives.isEmpty()) {
            final Directives dirs = new Directives().append(req.dirs());
            req = () -> {
                if (!map.isEmpty()) {
                    dirs.addIf("args");
                    for (final Entry<String, String> ent
                        : map.entrySet()) {
                        dirs.add("arg")
                            .attr("name", ent.getKey())
                            .set(ent.getValue())
                            .up();
                    }
                    dirs.up();
                }
                return dirs;
            };
        }
        return req;
    }

    /**
     * Fetch params from comment.
     * @param comment The comment
     * @return Map of params
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static Map<String, String> params(final Comment.Smart comment)
        throws IOException {
        final List<Entry<String, String>> entries = new LinkedList<>();
        final Matcher matcher = QnParametrized.PTN.matcher(comment.body());
        while (matcher.find()) {
            entries.add(
                new MapEntry<>(
                    matcher.group(1), matcher.group(2)
                )
            );
        }
        return new MapOf<>(new ListOf<>(entries));
    }

}
