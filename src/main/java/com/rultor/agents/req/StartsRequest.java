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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Merges.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "profile")
public final class StartsRequest extends AbstractAgent {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param prof Profile
     */
    public StartsRequest(final Profile prof) {
        super(
            "/talk/request[@id and type and not(success)]",
            "/talk[not(daemon)]"
        );
        this.profile = prof;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final XML req = xml.nodes("//request").get(0);
        final String type = req.xpath("type/text()").get(0);
        final String hash = req.xpath("@id").get(0);
        String script;
        try {
            script = this.script(req, type);
            Logger.info(this, "request %s/%s started", type, hash);
        } catch (final Profile.ConfigException ex) {
            script = Logger.format(
                "cat <<EOT\n%[exception]s\nEOT\nexit -1", ex
            );
        }
        return new Directives().xpath("/talk")
            .add("daemon")
            .attr("id", hash)
            .add("title").set(type).up()
            .add("script").set(script);
    }

    /**
     * Make a script.
     * @param req Request
     * @param type Its type
     * @return Script
     * @throws IOException If fails
     */
    private String script(final XML req, final String type) throws IOException {
        return StringUtils.join(
            Iterables.concat(
                Iterables.transform(
                    this.vars(req, type).entrySet(),
                    new Function<Map.Entry<String, String>, String>() {
                        @Override
                        public String apply(
                            final Map.Entry<String, String> input) {
                            return String.format(
                                "%s=%s", input.getKey(),
                                input.getValue()
                            );
                        }
                    }
                ),
                Collections.singleton(
                    IOUtils.toString(
                        this.getClass().getResourceAsStream("_head.sh"),
                        CharEncoding.UTF_8
                    )
                ),
                this.decrypt(),
                Collections.singleton(
                    IOUtils.toString(
                        this.getClass().getResourceAsStream(
                            String.format("%s.sh", type)
                        ),
                        CharEncoding.UTF_8
                    )
                )
            ),
            "\n"
        );
    }

    /**
     * Get variables from script.
     * @param req Request
     * @param type Its type
     * @return Vars
     * @throws IOException If fails
     */
    private Map<String, String> vars(final XML req, final String type)
        throws IOException {
        final ImmutableMap.Builder<String, String> vars =
            new ImmutableMap.Builder<String, String>();
        for (final XML arg : req.nodes("args/arg")) {
            vars.put(arg.xpath("@name").get(0), arg.xpath("text()").get(0));
        }
        final DockerRun docker = new DockerRun(
            this.profile, String.format("/p/%s", type)
        );
        vars.put("vars", docker.envs(vars.build()));
        vars.put(
            "image",
            new Profile.Defaults(this.profile).text(
                "/p/docker/image/text()", "yegor256/rultor"
            )
        );
        vars.put("scripts", docker.script());
        return vars.build();
    }

    /**
     * Decrypt instructions.
     * @return Instructions
     * @throws IOException If fails
     */
    private Iterable<String> decrypt() throws IOException {
        final Collection<String> commands = new LinkedList<String>();
        for (final XML node : this.profile.read().nodes("/p/decrypt/*")) {
            commands.add(
                Joiner.on(' ').join(
                    "gpg '--keyring=$(pwd)/.gpg/pubring.gpg'",
                    "'--secret-keyring=$(pwd)/.gpg/secring.gpg'",
                    String.format(
                        "--decrypt '%s' > '%s'",
                        node.xpath("./text()").get(0),
                        node.xpath("./name()").get(0)
                    )
                )
            );
        }
        commands.add("rm -rf .gpg");
        return commands;
    }

}
