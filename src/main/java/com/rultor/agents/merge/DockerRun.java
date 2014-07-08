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
package com.rultor.agents.merge;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
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
@EqualsAndHashCode(of = { "profile", "node" })
final class DockerRun {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Node name.
     */
    private final transient String node;

    /**
     * Ctor.
     * @param prof Profile
     * @param name Node name in profile XML
     */
    DockerRun(final Profile prof, final String name) {
        this.profile = prof;
        this.node = name;
    }

    /**
     * Make a script to run.
     * @return Script
     * @throws IOException If fails
     */
    public String script() throws IOException {
        final XML xml = this.profile.read().nodes(this.node).get(0);
        final Collection<String> scripts = new LinkedList<String>();
        if (!xml.nodes("script").isEmpty()) {
            if (xml.nodes("script/item").isEmpty()) {
                scripts.add(xml.xpath("script/text()").get(0));
            } else {
                boolean first = true;
                for (final String cmd : xml.xpath("script/item/text()")) {
                    scripts.add(cmd);
                    scripts.add(";");
                }
            }
        }
        return this.enlist(scripts);
    }

    /**
     * Make a list of env vars for docker.
     * @return Envs
     * @throws IOException If fails
     */
    public String envs() throws IOException {
        final XML xml = this.profile.read().nodes(this.node).get(0);
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
        return this.enlist(envs);
    }

    /**
     * Make a list for bash.
     * @param items Items
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

}
