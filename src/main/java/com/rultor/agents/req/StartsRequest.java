/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
            script = this.script(
                req, type, xml.xpath("/talk/@name").get(0)
            );
            Logger.info(
                this, "request %s/%s started for %s",
                type, hash, xml.xpath("/talk/@name ").get(0)
            );
        } catch (final Profile.ConfigException ex) {
            script = Logger.format(
                "cat <<EOT%n%[exception]s%nEOT%nexit -1", ex
            );
        }
        return new Directives().xpath("/talk")
            .add("daemon")
            .attr("id", hash)
            .add("title").set(type).up()
            .add("script").set(script);
    }

    @SuppressWarnings("unchecked")
    private String script(final XML req, final String type, final String name)
        throws IOException {
        return String.join(
            System.lineSeparator(),
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

    private String sensitive() throws IOException {
        String script = "";
        if (!this.profile.read().nodes("/p/entry[@key='release']").isEmpty()) {
            script = String.format(
                "sensitive=(%s)%n",
                String.join(
                    " ",
                    new Mapped<>(
                        Ssh::escape,
                        this.profile.read().xpath(
                            "/p/entry[@key='release']/entry[@key='sensitive']/item/text()"
                        )
                    )
                )
            );
        }
        return script;
    }

    private String asRoot() throws IOException {
        return String.format(
            "as_root=%b",
            !this.profile.read().nodes(
                "/p/entry[@key='docker']/entry[@key='as_root' and .='true']"
            ).isEmpty()
        );
    }

    private Map<String, String> vars(final XML req, final String type)
        throws IOException {
        final Collection<Map.Entry<String, String>> entries =
            new ArrayList<>(8);
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
                            input.replace(System.lineSeparator(), " ")
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
                        "false"
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
                        "false"
                    ).toLowerCase(Locale.ENGLISH)
                )
            );
        }
        return new MapOf<>(new ListOf<>(entries));
    }

    private DockerRun docker(final String type) throws IOException {
        final Collection<XML> nodes = this.profile.read().nodes(
            String.format("/p/entry[@key='%s']", type)
        );
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

    private static String escape(final String key, final String raw) {
        final String esc;
        if ("scripts".equals(key) || "vars".equals(key)) {
            esc = raw;
        } else {
            esc = Ssh.escape(raw);
        }
        return esc;
    }

    private static Iterable<String> export(final Iterable<String> envs) {
        final Collection<String> lines = new ArrayList<>(4);
        for (final String env : envs) {
            lines.add(String.format("export %s", Ssh.escape(env)));
            lines.add(";");
        }
        return lines;
    }
}
