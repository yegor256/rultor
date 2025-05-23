/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.req;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Sticky;
import org.cactoos.list.ListOf;
import org.cactoos.text.Split;
import org.cactoos.text.Trimmed;

/**
 * Docker run command.
 *
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "command" })
@SuppressWarnings("PMD.TooManyMethods")
final class DockerRun {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * XML element inside ".rultor.yml" with the command.
     */
    private final transient XML command;

    /**
     * Ctor.
     * @param prof Profile
     * @param xpath XPath of the XML element inside .rultor.yml
     * @throws IOException If fails
     */
    DockerRun(final Profile prof, final String xpath) throws IOException {
        this(prof, prof.read().nodes(xpath).iterator().next());
    }

    /**
     * Ctor.
     * @param prof Profile
     * @param node XML element inside ".rultor.yml" with the command
     */
    DockerRun(final Profile prof, final XML node) {
        this.profile = prof;
        this.command = node;
    }

    /**
     * Make a script to run.
     * @return Script
     * @throws IOException If fails
     */
    @SuppressWarnings("unchecked")
    public Iterable<String> script() throws IOException {
        final Iterable<String> trap;
        if (this.profile.read().nodes("/p/entry[@key='uninstall']").isEmpty()) {
            trap = Collections.emptyList();
        } else {
            trap = new Joined<>(
                new Sticky<>("function", "clean_up()", "{"),
                DockerRun.scripts(
                    this.profile.read(), "/p/entry[@key='uninstall']"
                ),
                new Sticky<>("}", ";"),
                new Sticky<>("trap", "clean_up", "EXIT", ";")
            );
        }
        return new Joined<>(
            trap,
            DockerRun.scripts(
                this.profile.read(), "/p/entry[@key='install']"
            ),
            DockerRun.scripts(this.command, "entry[@key='script']")
        );
    }

    /**
     * Make a list of env vars for docker.
     * @param extra Extra vars
     * @return Envs
     * @throws IOException If fails
     */
    @SuppressWarnings("unchecked")
    public Iterable<String> envs(final Map<String, String> extra)
        throws IOException {
        final List<String> entries = new LinkedList<>();
        for (final Entry<String, String> ent : extra.entrySet()) {
            entries.add(
                String.format(
                    "%s=%s", ent.getKey(), ent.getValue()
                )
            );
        }
        return new Joined<>(
            DockerRun.envs(this.profile.read(), "/p/entry[@key='env']"),
            DockerRun.envs(this.command, "entry[@key='env']"),
            new Sticky<>(entries)
        );
    }

    /**
     * Get items from XML.
     * @param xml The XML
     * @param path The XPath
     * @return Items
     */
    private static Iterable<String> scripts(final XML xml, final String path) {
        final Collection<String> items = new LinkedList<>();
        if (!xml.nodes(path).isEmpty()) {
            final XML node = xml.nodes(path).get(0);
            if (node.nodes("item").isEmpty()) {
                items.addAll(DockerRun.lines(node));
            } else {
                for (final String cmd : node.xpath("item/text()")) {
                    items.add(cmd.trim());
                }
            }
        }
        final Collection<String> scripts = new LinkedList<>();
        for (final String item : items) {
            scripts.add(neutralize(item));
            scripts.add(";");
        }
        return scripts;
    }

    /**
     * Is hash character inside double or single quotes.
     * @param item String to check.
     * @param pos Position of the hash.
     * @return If hash is in quotes.
     */
    private static boolean inquotes(final String item, final int pos) {
        final String sub = item.substring(0, pos);
        return sub.chars().filter(c -> c == '"').count() % 2 == 1
            || sub.chars().filter(c -> c == '\'').count() % 2 == 1;
    }

    /**
     * Neutralize comment contained in the script line.
     * @param item Script element.
     * @return Script element with invisible comment
     */
    private static String neutralize(final String item) {
        final int start = item.indexOf('#');
        final String result;
        if (start == 0 || start > 0 && item.charAt(start - 1) != '\\'
            && !DockerRun.inquotes(item, start)) {
            result = new StringBuilder(item.substring(0, start))
                .append('`')
                .append(item.substring(start))
                .append('`')
                .toString();
        } else {
            result = item;
        }
        return result;
    }

    /**
     * Environments from XML by XPath.
     * @param xml The XML
     * @param path The XPath
     * @return Items
     */
    private static Iterable<String> envs(final XML xml, final String path) {
        final Collection<String> envs = new LinkedList<>();
        if (!xml.nodes(path).isEmpty()) {
            final XML node = xml.nodes(path).get(0);
            final Collection<String> parts;
            if (node.nodes("item").iterator().hasNext()) {
                parts = node.xpath("item/text()");
            } else if (node.nodes("entry").iterator().hasNext()) {
                parts = new LinkedList<>();
                for (final XML env : node.nodes("./entry")) {
                    parts.add(
                        String.format(
                            "%s=%s", env.xpath("@key").get(0),
                            env.xpath("text()").get(0)
                        )
                    );
                }
            } else {
                parts = DockerRun.lines(node);
            }
            envs.addAll(
                new ListOf<>(
                    new Mapped<>(
                        input -> String.format("%s", input),
                        parts
                    )
                )
            );
        }
        return envs;
    }

    /**
     * Get lines from a single XML node.
     * @param node Node to get text() from
     * @return Lines found
     */
    private static Collection<String> lines(final XML node) {
        final Collection<String> lines = new LinkedList<>();
        if (node.inner().hasChildNodes()) {
            final List<String> src = new ListOf<String>(
                new Mapped<>(
                    t -> new Trimmed(t).asString(),
                    new Split(node.xpath("text()").get(0), "\n")
                )
            );
            for (final String str : src) {
                if (!str.isEmpty()) {
                    lines.add(str);
                }
            }
        }
        return lines;
    }

}
