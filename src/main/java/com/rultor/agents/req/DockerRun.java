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
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Docker run command.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
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
        return this.enlist(
            Iterables.concat(
                this.items(this.profile.read(), "p/install"),
                this.items(this.node(), "script")
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
        final XML xml = this.node();
        final Collection<String> envs = new LinkedList<String>();
        if (!xml.nodes("env").isEmpty()) {
            final Collection<String> parts;
            if (xml.nodes("env/item").iterator().hasNext()) {
                parts = xml.xpath("env/item/text()");
            } else if (xml.nodes("env/*/text()").iterator().hasNext()) {
                parts = new LinkedList<String>();
                for (final XML env : xml.nodes("env/*")) {
                    parts.add(
                        String.format(
                            "%s=%s", env.xpath("name()").get(0),
                            env.xpath("text()").get(0)
                        )
                    );
                }
            } else {
                parts = Collections.singleton(xml.xpath("env/text()").get(0));
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
        envs.addAll(
            Collections2.transform(
                extra.entrySet(),
                new Function<Map.Entry<String, String>, String>() {
                    @Override
                    public String apply(final Map.Entry<String, String> ent) {
                        return String.format(
                            "--env=%s=%s", ent.getKey(), ent.getValue()
                        );
                    }
                }
            )
        );
        return this.enlist(envs);
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
    private String enlist(final Iterable<String> items) {
        return String.format(
            "( %s )",
            Joiner.on(' ').join(
                Iterables.transform(
                    items,
                    new Function<String, String>() {
                        @Override
                        public String apply(final String input) {
                            return String.format(
                                "'%s'",
                                input.trim().replace("'", "'\\''")
                            );
                        }
                    }
                )
            )
        );
    }

    /**
     * Get items from XML.
     * @param xml The XML
     * @param xpath The XPath
     * @return Items
     */
    private Iterable<String> items(final XML xml, final String xpath) {
        final Collection<String> items = new LinkedList<String>();
        if (!xml.nodes(xpath).isEmpty()) {
            if (xml.nodes(String.format("%s/item", xpath)).isEmpty()) {
                items.add(xml.xpath(String.format("%s/text()", xpath)).get(0));
            } else {
                for (final String cmd
                    : xml.xpath(String.format("%s/item/text()", xpath))) {
                    items.add(cmd);
                    items.add(";");
                }
            }
        }
        return items;
    }

}
