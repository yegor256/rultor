/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.jcabi.github.RtGithub;
import com.jcabi.github.wire.RetryCarefulWire;
import com.jcabi.xml.XML;
import com.rultor.Env;
import com.rultor.agents.github.TalkIssues;
import com.rultor.agents.github.qtn.DefaultBranch;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Profiles.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class Profiles {

    /**
     * Merge command.
     */
    private static final String MERGE = "merge";

    /**
     * Fetch a profile from a talk.
     * @param talk The talk
     * @return Profile found
     * @throws IOException If fails
     */
    public Profile fetch(final Talk talk) throws IOException {
        final Profile profile;
        if (Talk.TEST_NAME.equals(talk.name())) {
            profile = new Profile.Fixed();
        } else {
            final XML xml = talk.read();
            if (xml.nodes("/talk/wire").isEmpty()) {
                profile = Profile.EMPTY;
            } else {
                profile = this.fetch(xml);
            }
        }
        return profile;
    }

    /**
     * Merge profile from master and fork. Merged profile must be profile
     * from fork, but all lists for commanders and architects must be taken
     * from master branch.
     * @param master Profile from master branch
     * @param fork Fork that will be merged
     * @param branch Fork branch that will be merged
     * @return Merged profile
     * @checkstyle NonStaticMethodCheck (5 lines)
     */
    public Profile merged(final Profile master, final String fork,
        final String branch) {
        return master;
    }

    /**
     * Validate merged profile: merged profile must have no changes in architect
     * and commander sections (full match with master profile).
     * @param master Profile from master branch
     * @param merged Result of merging master and fork profiles
     * @return Validated profile
     * @throws IOException If fails
     * @todo #1039:30min Add integration test to  be sure that validation
     *  exception will eventually be posted as an Answer in GitHub.
     *  After you create integration test, add `validated` method call in
     *  `fetch` method: `...this.validated(master, this.merged(...`.
     *  Modify `merged` method implementation: merged profile is a
     *  `.rultor.yml` form result of real `git merge` of master branch and
     *  fork branch. We need real `git merge` result here. See some details
     *  here: https://github.com/yegor256/rultor/pull/1064/files#r56796959
     * @checkstyle NonStaticMethodCheck (15 lines)
     */
    public Profile validated(final Profile master, final Profile merged)
        throws IOException {
        final String commanders = "commanders";
        final String[][] security = {
            {"architect"},
            {Profiles.MERGE, commanders},
            {"deploy", commanders},
            {"release", commanders},
        };
        for (final String[] section : security) {
            if (!Arrays.equals(
                Profiles.section(master, section),
                Profiles.section(merged, section)
            )) {
                throw new Profile.ConfigException(
                    String.format(
                        "You cannot change `%s` section for security reasons",
                        Arrays.toString(section)
                    )
                );
            }
        }
        return merged;
    }

    /**
     * Get section content.
     * @param profile Profile
     * @param section Section of profile
     * @return Content of section as array of strings
     * @throws IOException If fails
     */
    private static String[] section(final Profile profile,
        final String... section)
        throws IOException {
        final StringBuilder path = new StringBuilder(100);
        for (final String element : section) {
            path.append(String.format("/entry[@key='%s']", element));
        }
        final List<String> result = profile.read().xpath(
            String.format("/%s/item/text()", path)
        );
        return result.toArray(new String[0]);
    }

    /**
     * Fetch a profile from an XML.
     * @param xml The XML
     * @return Profile found
     */
    private Profile fetch(final XML xml) {
        final Profile profile;
        final List<String> type = xml.xpath("//request/type/text()");
        if (type.isEmpty() || !Profiles.MERGE.equals(type.get(0))) {
            final Repo repo = new TalkIssues(
                Profiles.github(), xml
            ).get().repo();
            profile = new GithubProfile(
                repo,
                new DefaultBranch(repo).toString()
            );
        } else {
            profile = this.merged(
                new GithubProfile(
                    new TalkIssues(Profiles.github(), xml).get().repo()
                ),
                xml.xpath("//request/args/arg[@name='fork']/text()").get(0),
                xml.xpath(
                    "//request/args/arg[@name='fork_branch']/text()"
                ).get(0)
            );
        }
        return profile;
    }

    /**
     * Make github.
     * @return Github
     */
    @Cacheable(forever = true)
    private static Github github() {
        return new RtGithub(
            new RtGithub(
                Env.read("Rultor-GithubToken")
            ).entry().through(
                RetryCarefulWire.class,
                100
            )
        );
    }

}
