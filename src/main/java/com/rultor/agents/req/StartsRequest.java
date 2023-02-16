/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
import com.jcabi.log.Logger;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.daemons.Container;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Merges.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "profile")
@SuppressWarnings("PMD.ExcessiveMethodLength")
public final class StartsRequest extends AbstractAgent {

    /**
     * Default port value to be used with Decrypt.
     */
    private static final String DEFAULT_PORT = "80";
    /**
     * HTTP proxy port system property key.
     */
    private static final String PORT_KEY = "http.proxyPort";
    /**
     * HTTP proxy host system property key.
     */
    private static final String HOST_KEY = "http.proxyHost";

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
            script = this.script(
                req, type, xml.xpath("/talk/@name").get(0)
            );
            Logger.info(
                this, "request %s/%s started for %s",
                type, hash, xml.xpath("/talk/@name ").get(0)
            );
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
     * @param name Name of talk
     * @return Script
     * @throws IOException If fails
     */
    @SuppressWarnings("unchecked")
    private String script(final XML req, final String type, final String name)
        throws IOException {
        return String.join(
            "\n",
            new Joined<>(
                new Mapped<>(
                    input -> String.format(
                        "%s=%s", input.getKey(),
                        StartsRequest.escape(input.getKey(), input.getValue())
                    ),
                    new Joined<Map.Entry<String, String>>(
                        this.vars(req, type).entrySet(),
                        new MapOf<>(
                            new MapEntry<>(
                                "container",
                                new Container(name).toString()
                            )
                        ).entrySet()
                    )
                ),
                Collections.singleton(this.asRoot()),
                Collections.singleton(
                    IOUtils.toString(
                        this.getClass().getResourceAsStream("_head.sh"),
                        StandardCharsets.UTF_8
                    )
                ),
                Collections.singleton(this.sensitive()),
                this.decryptor().commands(),
                Collections.singleton(
                    IOUtils.toString(
                        this.getClass().getResourceAsStream(
                            String.format("%s.sh", type)
                        ),
                        StandardCharsets.UTF_8
                    )
                )
            )
        );
    }

    /**
     * Obtain proxy settings and create a Decrypt instance.
     * @return Decrypt instance.
     */
    private Decrypt decryptor() {
        return new Decrypt(
            this.profile,
            System.getProperty(HOST_KEY, ""),
            Integer.parseInt(System.getProperty(PORT_KEY, DEFAULT_PORT))
        );
    }

    /**
     * List sensitive files in a BASH variable "sensitive".
     * @return Bash script
     * @throws IOException If fails
     */
    private String sensitive() throws IOException {
        String script = "";
        if (!this.profile.read().nodes("/p/entry[@key='release']").isEmpty()) {
            script = String.format(
                "sensitive=(%s)\n",
                String.join(
                    " ",
                    new Mapped<>(
                        Ssh::escape,
                        this.profile.read().xpath(
                            // @checkstyle LineLength (1 line)
                            "/p/entry[@key='release']/entry[@key='sensitive']/item/text()"
                        )
                    )
                )
            );
        }
        return script;
    }

    /**
     * Get start script for as_root config.
     * @return Script
     * @throws IOException If fails
     * @since 1.37
     */
    private String asRoot() throws IOException {
        return String.format(
            "as_root=%b",
            !this.profile.read().nodes(
                "/p/entry[@key='docker']/entry[@key='as_root' and .='true']"
            ).isEmpty()
        );
    }

    /**
     * Get variables from script.
     * @param req Request
     * @param type Its type
     * @return Vars
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Map<String, String> vars(final XML req, final String type)
        throws IOException {
        final Collection<Map.Entry<String, String>> entries =
            new LinkedList<>();
        for (final XML arg : req.nodes("args/arg")) {
            entries.add(
                new MapEntry<>(
                    arg.xpath("@name").get(0),
                    arg.xpath("text()").get(0)
                )
            );
        }
        entries.add(
            new MapEntry<>(
                "author", req.xpath("author/text()").get(0)
            )
        );
        final DockerRun docker = this.docker(type);
        entries.add(
            new MapEntry<>(
                "scripts",
                new Brackets(
                    new Joined<String>(
                        StartsRequest.export(
                            docker.envs(
                                new MapOf<>(
                                    new ListOf<>(entries)
                                )
                            )
                        ),
                        docker.script()
                    )
                ).toString()
            )
        );
        entries.add(
            new MapEntry<>(
                "vars",
                new Brackets(
                    new Mapped<>(
                        input -> String.format(
                            "--env=%s",
                            input.replace("\n", " ")
                        ),
                        docker.envs(
                            new MapOf<>(
                                new ListOf<>(entries)
                            )
                        )
                    )
                ).toString()
            )
        );
        final Profile.Defaults def = new Profile.Defaults(this.profile);
        entries.add(
            new MapEntry<>(
                "image",
                def.text(
                    "/p/entry[@key='docker']/entry[@key='image']",
                    "yegor256/rultor-image"
                )
            )
        );
        entries.add(
            new MapEntry<>(
                "directory",
                def.text("/p/entry[@key='docker']/entry[@key='directory']")
            )
        );
        if (!this.profile.read().nodes("/p/entry[@key='merge']").isEmpty()) {
            entries.add(
                new MapEntry<>(
                    "squash",
                    def.text(
                        "/p/entry[@key='merge']/entry[@key='squash']",
                        Boolean.FALSE.toString()
                    ).toLowerCase(Locale.ENGLISH)
                )
            );
            entries.add(
                new MapEntry<>(
                    "ff",
                    def.text(
                        "/p/entry[@key='merge']/entry[@key='fast-forward']",
                        "default"
                    ).toLowerCase(Locale.ENGLISH)
                )
            );
            entries.add(
                new MapEntry<>(
                    "rebase",
                    def.text(
                        "/p/entry[@key='merge']/entry[@key='rebase']",
                        Boolean.FALSE.toString()
                    ).toLowerCase(Locale.ENGLISH)
                )
            );
        }
        return new MapOf<>(new ListOf<>(entries));
    }

    /**
     * Get docker run config.
     * @param type Type of command, like 'release' or 'merge'
     * @return Docker run cfg
     * @throws IOException If fails
     */
    private DockerRun docker(final String type) throws IOException {
        final String xpath = String.format("/p/entry[@key='%s']", type);
        final Collection<XML> nodes = this.profile.read().nodes(xpath);
        if (nodes.isEmpty()) {
            throw new Profile.ConfigException(
                String.format(
                    "There is no '%s' section in %s for branch %s in repo %s",
                    type,
                    ".rultor.yml",
                    "master",
                    this.profile.name()
                )
            );
        }
        return new DockerRun(this.profile, nodes.iterator().next());
    }

    /**
     * Escape var.
     * @param key The name of the var
     * @param raw The variable
     * @return Escaped one
     */
    private static String escape(final String key, final String raw) {
        final String esc;
        if ("scripts".equals(key) || "vars".equals(key)) {
            esc = raw;
        } else {
            esc = Ssh.escape(raw);
        }
        return esc;
    }

    /**
     * Export env vars.
     * @param envs List of them
     * @return Formatted lines
     */
    private static Iterable<String> export(final Iterable<String> envs) {
        final Collection<String> lines = new LinkedList<>();
        for (final String env : envs) {
            lines.add(String.format("export %s", Ssh.escape(env)));
            lines.add(";");
        }
        return lines;
    }
}
