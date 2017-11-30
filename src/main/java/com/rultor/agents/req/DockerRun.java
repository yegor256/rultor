/**
 * Copyright (c) 2009-2018, rultor.com
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
package com.rultor.agents.req;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.iterable.Joined;
import org.cactoos.list.SolidList;

/**
 * Docker run command.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "xpath" })
@SuppressWarnings("PMD.TooManyMethods")
final class DockerRun {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Node name.
     */
    private final transient String xpath;

    /**
     * Ctor.
     * @param prof Profile
     * @param path Node name in profile XML, XPath
     */
    DockerRun(final Profile prof, final String path) {
        this.profile = prof;
        this.xpath = path;
    }

    /**
     * Make a script to run.
     * @return Script
     * @throws IOException If fails
     */
    public Iterable<String> script() throws IOException {
        final Iterable<String> trap;
        if (this.profile.read().nodes("/p/entry[@key='uninstall']").isEmpty()) {
            trap = Collections.emptyList();
        } else {
            trap = new Joined<String>(
                new SolidList<String>("function", "clean_up()", "{"),
                DockerRun.scripts(
                    this.profile.read(), "/p/entry[@key='uninstall']"
                ),
                new SolidList<String>("}", ";"),
                new SolidList<String>("trap", "clean_up", "EXIT", ";")
            );
        }
        return new Joined<String>(
            trap,
            DockerRun.scripts(
                this.profile.read(), "/p/entry[@key='install']"
            ),
            DockerRun.scripts(this.node(), "entry[@key='script']")
        );
    }

    /**
     * Make a list of env vars for docker.
     * @param extra Extra vars
     * @return Envs
     * @throws IOException If fails
     */
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
        return new Joined<String>(
            DockerRun.envs(this.profile.read(), "/p/entry[@key='env']"),
            DockerRun.envs(this.node(), "entry[@key='env']"),
            new SolidList<String>(entries)
        );
    }

    /**
     * Get xpath.
     * @return XML
     * @throws IOException If fails
     */
    private XML node() throws IOException {
        final XML node;
        final Collection<XML> nodes = this.profile.read().nodes(this.xpath);
        if (nodes.isEmpty()) {
            node = new XMLDocument("<x/>");
        } else {
            node = nodes.iterator().next();
        }
        return node;
    }

    /**
     * Get items from XML.
     * @param xml The XML
     * @param path The XPath
     * @return Items
     */
    private static Iterable<String> scripts(final XML xml, final String path) {
        final Collection<String> items = new LinkedList<String>();
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
        final Collection<String> scripts = new LinkedList<String>();
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
        return StringUtils.countMatches(item.substring(0, pos), "\"") % 2 == 1
            || StringUtils.countMatches(item.substring(0, pos), "'") % 2 == 1;
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
                new SolidList<String>(
                    new org.cactoos.collection.Mapped<>(
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
        final Collection<String> lines = new LinkedList<String>();
        if (node.node().hasChildNodes()) {
            lines.addAll(
                new SolidList<String>(
                    new org.cactoos.collection.Mapped<>(
                        input -> input.trim(),
                        Arrays.asList(
                            StringUtils.split(node.xpath("text()").get(0), '\n')
                        )
                    )
                )
            );
        }
        return lines;
    }

}
