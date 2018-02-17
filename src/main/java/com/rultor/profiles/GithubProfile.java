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
package com.rultor.profiles;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Content;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.lang3.CharEncoding;
import org.cactoos.collection.Mapped;
import org.cactoos.list.SolidList;
import org.cactoos.map.MapEntry;
import org.cactoos.map.SolidMap;
import org.cactoos.text.JoinedText;

/**
 * Github Profile.
 *
 * <p>An instance of this class is created by {@link Profiles}, when
 * it is obvious that the configuration of the repository is stored
 * in Github.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle AvoidInstantiatingObjectsInLoops
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "repo")
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
        this(rpo, "master");
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
        return new SolidMap<String, InputStream>(entries);
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
                String.format("invalid path of asset: %s", path)
            );
        }
        final Repo rpo = this.repo.github().repos().get(
            new Coordinates.Simple(matcher.group(1))
        );
        if (!rpo.contents().exists(GithubProfile.FILE, this.branch)) {
            throw new Profile.ConfigException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "`%s` file must be present in root directory of %s, see http://doc.rultor.com/reference.html#assets",
                    GithubProfile.FILE, rpo.coordinates()
                )
            );
        }
        final Collection<String> friends =
            new SolidList<>(
                new Mapped<>(
                    input -> input.toLowerCase(Locale.ENGLISH),
                    new YamlXML(
                        new String(
                            new Content.Smart(
                                rpo.contents().get(GithubProfile.FILE)
                            ).decoded(),
                            CharEncoding.UTF_8
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
                    "`%s` in %s doesn't allow %s to use its assets (there are %d friends), see http://doc.rultor.com/reference.html#assets",
                    GithubProfile.FILE, rpo.coordinates(),
                    this.repo.coordinates(), friends.size()
                )
            );
        }
        return this.buildAssetStream(rpo, matcher.group(2));
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
                    "`%s` on `%s` does not exist.",
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
                CharEncoding.UTF_8
            );
        } else {
            yml = "";
        }
        final List<String> msg = this.validate(yml);
        if (!msg.isEmpty()) {
            throw new Profile.ConfigException(
                String.format(
                    "`%s` is not valid according to schema:\n``%s``",
                    GithubProfile.FILE,
                    new JoinedText(
                        "\n",
                        msg
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
     *  from GitHubProfileValidationTest to ValidYamlTest. Remember about
     *  removing PMD suppress below.
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private List<String> validate(final String yml) {
        return Collections.emptyList();
    }
}
