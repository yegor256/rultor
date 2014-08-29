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
package com.rultor.agents.req;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.agents.shells.SSH;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

/**
 * Docker run command.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "profile", "xpath" })
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
    public String script() throws IOException {
        return DockerRun.enlist(
            Iterables.concat(
                DockerRun.scripts(
                    this.profile.read(), "/p/entry[@key='install']"
                ),
                DockerRun.scripts(this.node(), "entry[@key='script']")
            )
        );
    }

    /**
     * Make a list of env vars for docker.
     * @param extra Extra vars
     * @return Envs
     * @throws IOException If fails
     */
    public String envs(final Map<String, String> extra) throws IOException {
        return DockerRun.enlist(
            Iterables.concat(
                DockerRun.envs(this.profile.read(), "/p/entry[@key='env']"),
                DockerRun.envs(this.node(), "entry[@key='env']"),
                Collections2.transform(
                    extra.entrySet(),
                    new Function<Map.Entry<String, String>, String>() {
                        @Override
                        public String apply(
                            final Map.Entry<String, String> ent) {
                            return String.format(
                                "--env=%s=%s", ent.getKey(), ent.getValue()
                            );
                        }
                    }
                )
            )
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
     * Make a list for bash.
     * @param items Items
     * @return Text for bash
     */
    private static String enlist(final Iterable<String> items) {
        return String.format(
            "( %s )",
            Joiner.on(' ').join(
                Iterables.transform(
                    items,
                    new Function<String, String>() {
                        @Override
                        public String apply(final String input) {
                            return SSH.escape(input);
                        }
                    }
                )
            )
        );
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
            scripts.add(item);
            scripts.add(";");
        }
        return scripts;
    }

    /**
     * Environments from XML by XPath.
     * @param xml The XML
     * @param path The XPath
     * @return Items
     */
    private static Iterable<String> envs(final XML xml, final String path) {
        final Collection<String> envs = new LinkedList<String>();
        if (!xml.nodes(path).isEmpty()) {
            final XML node = xml.nodes(path).get(0);
            final Collection<String> parts;
            if (node.nodes("item").iterator().hasNext()) {
                parts = node.xpath("item/text()");
            } else if (node.nodes("entry").iterator().hasNext()) {
                parts = new LinkedList<String>();
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
                Collections2.transform(
                    parts,
                    new Function<String, String>() {
                        @Override
                        public String apply(final String input) {
                            return String.format("--env=%s", input);
                        }
                    }
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
        return Collections2.transform(
            Arrays.asList(
                StringUtils.split(node.xpath("text()").get(0), '\n')
            ),
            new Function<String, String>() {
                @Override
                public String apply(final String line) {
                    return line.trim();
                }
            }
        );
    }

}
