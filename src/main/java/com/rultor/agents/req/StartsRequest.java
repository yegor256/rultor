/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import java.util.Objects;
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
 * @since 1.0
 * @checkstyle MultipleStringLiteralsCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "profile")
@SuppressWarnings("PMD.ExcessiveMethodLength")
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
     * @param req Request in XML
     * @param type Its type, like "merge", "deploy", or "release"
     * @param name Name of the talk
     * @return Bash script to run on the server
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
                        Objects.requireNonNull(this.getClass().getResource("_head.sh")),
                        StandardCharsets.UTF_8
                    )
                ),
                Collections.singleton(this.sensitive()),
                new Decrypt(this.profile).commands(),
                Collections.singleton(
                    IOUtils.toString(
                        Objects.requireNonNull(
                            this.getClass().getResource(String.format("%s.sh", type))
                        ),
                        StandardCharsets.UTF_8
                    )
                )
            )
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
     * @param type Its type, like "merge", "deploy", or "release"
     * @return Vars
     * @throws IOException If fails
     */
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
                    this.profile.defaultBranch(),
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
