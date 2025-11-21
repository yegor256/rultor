/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Content;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Repo;
import com.jcabi.github.RepoCommit;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.github.qtn.DefaultBranch;
import com.rultor.spi.Profile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.map.MapEntry;
import org.cactoos.map.MapOf;
import org.cactoos.text.Joined;
import org.cactoos.text.UncheckedText;

/**
 * GitHub Profile.
 *
 * <p>An instance of this class is created by {@link Profiles}, when
 * it is obvious that the configuration of the repository is stored
 * in GitHub.
 *
 * @since 1.0
 * @checkstyle AvoidInstantiatingObjectsInLoops
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "repo")
@SuppressWarnings("PMD.ExcessiveImports")
final class GithubProfile implements Profile {

    /**
     * File name.
     */
    private static final String FILE = ".rultor.yml";

    /**
     * Path pattern.
     */
    private static final Pattern PATH = Pattern.compile(
        "([a-zA-Z0-9][a-zA-Z0-9-]*/[a-zA-Z_0-9.-]+)#(.+)"
    );

    /**
     * Repo.
     */
    private final transient Repo repo;

    /**
     * Branch name.
     */
    private final transient String branch;

    /**
     * Ctor.
     * @param rpo Repo
     */
    GithubProfile(final Repo rpo) {
        this(rpo, new DefaultBranch(rpo).toString());
    }

    /**
     * Ctor.
     * @param rpo Repo
     * @param brnch Branch
     * @since 1.51
     */
    GithubProfile(final Repo rpo, final String brnch) {
        this.repo = rpo;
        this.branch = brnch;
    }

    @Override
    public String name() {
        return this.repo.coordinates().toString();
    }

    @Override
    public String defaultBranch() {
        return this.branch;
    }

    @Override
    public XML read() throws IOException {
        return new YamlXML(this.yml()).get();
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Override
    public Map<String, InputStream> assets() throws IOException {
        final XML xml = this.read();
        final List<XML> nodes = xml.nodes("/p/entry[@key='assets']/entry");
        final List<Entry<String, InputStream>> entries = new LinkedList<>();
        for (final XML node : nodes) {
            entries.add(
                new MapEntry<>(
                    node.xpath("@key").get(0),
                    this.asset(node.xpath("text()").get(0))
                )
            );
        }
        return new MapOf<>(new ListOf<>(entries));
    }

    /**
     * Convert address to input stream.
     * @param path Path of the asset, e.g. "yegor/rultor#pom.xml"
     * @return Stream with content
     * @throws IOException If fails
     */
    private InputStream asset(final String path) throws IOException {
        final Matcher matcher = GithubProfile.PATH.matcher(path);
        if (!matcher.matches()) {
            throw new Profile.ConfigException(
                String.format("Invalid path of asset: %s", path)
            );
        }
        final Repo rpo = this.repo.github().repos().get(
            new Coordinates.Simple(matcher.group(1))
        );
        if (!rpo.contents().exists(GithubProfile.FILE, this.branch)) {
            throw new Profile.ConfigException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "%s file must be present in root directory of %s, see https://doc.rultor.com/reference.html#assets",
                    GithubProfile.FILE, rpo.coordinates()
                )
            );
        }
        final Collection<String> friends = new ListOf<>(
            new Mapped<>(
                input -> input.toLowerCase(Locale.ENGLISH),
                new YamlXML(
                    new String(
                        new Content.Smart(
                            rpo.contents().get(GithubProfile.FILE)
                        ).decoded(),
                        StandardCharsets.UTF_8
                    )
                ).get().xpath("/p/entry[@key='friends']/item/text()")
            )
        );
        final String coords = this.repo.coordinates()
            .toString().toLowerCase(Locale.ENGLISH);
        if (!friends.contains(coords)) {
            throw new Profile.ConfigException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "%s in %s doesn't allow %s to use its assets (there are %d friends), see http://doc.rultor.com/reference.html#assets",
                    GithubProfile.FILE, rpo.coordinates(),
                    this.repo.coordinates(), friends.size()
                )
            );
        }
        this.checkTrustees(rpo);
        return this.buildAssetStream(rpo, matcher.group(2));
    }

    /**
     * Check that everything is OK with trustees.
     * @param rpo The repo
     * @throws IOException If fails
     */
    @SuppressWarnings("unchecked")
    private void checkTrustees(final Repo rpo) throws IOException {
        final Collection<String> trustees =
            new ListOf<>(
                new Mapped<>(
                    input -> input.toLowerCase(Locale.ENGLISH),
                    new YamlXML(
                        new String(
                            new Content.Smart(
                                rpo.contents().get(GithubProfile.FILE)
                            ).decoded(),
                            StandardCharsets.UTF_8
                        )
                    ).get().xpath("/p/entry[@key='trustees']/item/text()")
                )
            );
        if (!trustees.isEmpty()) {
            final Iterable<RepoCommit> commits = this.repo.commits().iterate(
                new MapOf<>(
                    new MapEntry<>("path", GithubProfile.FILE)
                )
            );
            if (!commits.iterator().hasNext()) {
                throw new Profile.ConfigException(
                    String.format(
                        "Couldn't find %s in %s",
                        GithubProfile.FILE, this.repo.coordinates()
                    )
                );
            }
            final RepoCommit.Smart commit = new RepoCommit.Smart(
                commits.iterator().next()
            );
            if (!commit.isVerified()) {
                throw new Profile.ConfigException(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "The last commit at %s in %s is not verified, that's why assets are not permitted to use in %s",
                        GithubProfile.FILE, this.repo.coordinates(),
                        rpo.coordinates()
                    )
                );
            }
            final String author = commit.author();
            if (!trustees.contains(author)) {
                throw new Profile.ConfigException(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "Since @%s is the modifier of %s in %s, that's why assets are not permitted to use in %s",
                        author, GithubProfile.FILE, this.repo.coordinates(),
                        rpo.coordinates()
                    )
                );
            }
        }
    }

    /**
     * Build the InputStream for the given filename in the given Repository,
     * dealing with errors.
     * @param rpo Repository where the file is.
     * @param filename Name of the file.
     * @return An InputStream with the Base64 contents of the file.
     * @throws IOException If something goes wrong.
     */
    private InputStream buildAssetStream(final Repo rpo, final String filename)
        throws IOException {
        if (!rpo.contents().exists(filename, this.branch)) {
            throw new Profile.ConfigException(
                String.format(
                    "%s on %s does not exist.",
                    filename, this.branch
                )
            );
        }
        return new ByteArrayInputStream(
            Base64.decodeBase64(
                new Content.Smart(
                    rpo.contents().get(filename)
                ).content()
            )
        );
    }

    /**
     * Get .rultor.yml file.
     * @return Its content
     * @throws IOException If fails
     */
    private String yml() throws IOException {
        final String yml;
        if (this.repo.contents()
            .exists(GithubProfile.FILE, this.branch)) {
            yml = new String(
                new Content.Smart(
                    this.repo.contents().get(GithubProfile.FILE)
                ).decoded(),
                StandardCharsets.UTF_8
            );
        } else {
            Logger.debug(
                this,
                "There is no '%s' file in '%s' repository (branch '%s')",
                GithubProfile.FILE, this.repo, this.branch
            );
            yml = "";
        }
        final List<String> msg = this.validate(yml);
        if (!msg.isEmpty()) {
            throw new Profile.ConfigException(
                String.format(
                    "%s is not valid according to schema:\n``%s``",
                    GithubProfile.FILE,
                    new UncheckedText(
                        new Joined(
                            "\n",
                            msg
                        )
                    ).asString()
                )
            );
        }
        return yml;
    }

    /**
     * Validate rultor config YAML according to schema.
     * @param yml Rultor YAML config
     * @return Validation result message, empty list means validation succeeded.
     * @todo #570:30min Implement validation using Kwalify library in separate
     *  class called ValidYaml, move this method to that class and move tests
     *  from GithubProfileValidationTest to ValidYamlTest. Remember about
     *  removing PMD suppress below.
     * @checkstyle NonStaticMethodCheck (5 lines)
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private List<String> validate(final String yml) {
        return Collections.emptyList();
    }
}
