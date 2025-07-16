/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.github.Issue;
import com.jcabi.github.Releases;
import com.jcabi.github.Repo;
import com.jcabi.github.mock.MkGithub;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.LengthOf;
import org.cactoos.text.Joined;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xembly.Directives;

/**
 * Tests for {@link ReleaseBinaries}.
 *
 * @since 1.1
 */
final class ReleaseBinariesTest {

    /**
     * ReleaseBinaries should attach artifact to release.
     * @param temp Temporary folder for talk
     * @throws Exception In case of error
     */
    @Test
    void attachesBinaryToRelease(
        @TempDir final Path temp
    ) throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final String tag = "v1.0";
        final String target = "target";
        final String name = "name-${tag}.jar";
        final File dir = new File(
            String.join(
                File.pathSeparator,
                temp.toFile().getAbsolutePath(), "repo", target
            )
        );
        final File bin = new File(dir.getAbsolutePath(), name.replace("${tag}", tag));
        final byte[] content = SecureRandom.getSeed(100);
        new LengthOf(new TeeInput(content, bin)).value();
        final Talk talk = ReleaseBinariesTest
            .talk(repo.issues().create("", ""), tag, dir);
        new CommentsTag(repo.github()).execute(talk);
        new ReleaseBinaries(
            repo.github(),
            new Profile.Fixed(
                new XMLDocument(
                    new Joined(
                        "",
                        "<p><entry key='release'><entry key='artifacts'>",
                        target, "/", name,
                        "</entry></entry></p>"
                    ).asString()
                )
            )
        ).execute(talk);
        MatcherAssert.assertThat(
            "Asset url should be in the release",
            new Releases.Smart(repo.releases()).find(tag)
                .assets().get(0),
            Matchers.notNullValue()
        );
    }

    /**
     * ReleaseBinaries should attach binaries from a directory to the release.
     * @param temp Temporary folder for talk
     * @throws Exception In case of error
     */
    @Test
    void attachesBinariesFromDirectory(@TempDir final Path temp) throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final String tag = "v1.0";
        final Talk reltalk = ReleaseBinariesTest.talk(issue, tag, temp.toFile());
        new CommentsTag(repo.github()).execute(reltalk);
        final File work = temp.resolve("repo").resolve("target").toFile();
        final String filename = String.format("artifact-%s.jar", tag);
        final File artifact = new File(work, filename);
        final byte[] data = SecureRandom.getSeed(32);
        new LengthOf(new TeeInput(data, artifact)).value();
        final Profile prof = new Profile.Fixed(
            new XMLDocument(
                new Joined(
                    "",
                    "<p><entry key='artifacts'>target</entry></p>"
                ).asString()
            )
        );
        final Talk dirtalk = ReleaseBinariesTest.talk(issue, tag, temp.toFile());
        new ReleaseBinaries(repo.github(), prof).execute(dirtalk);
        MatcherAssert.assertThat(
            "Asset should be attached",
            new Releases.Smart(repo.releases()).find(tag).assets().get(0),
            Matchers.notNullValue()
        );
    }

    /**
     * ReleaseBinaries should attach a single artifact file to the release.
     * @param temp Temporary folder for talk
     * @throws Exception In case of error
     */
    @Test
    void attachesSingleBinaryFile(@TempDir final Path temp) throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final Issue issue = repo.issues().create("", "");
        final String tag = "v2.0";
        final Talk inittalk = ReleaseBinariesTest.talk(issue, tag, temp.toFile());
        new CommentsTag(repo.github()).execute(inittalk);
        final File file = temp.resolve(
            String.format("release-%s.zip", tag)
        ).toFile();
        final byte[] data = SecureRandom.getSeed(16);
        new LengthOf(new TeeInput(data, file)).value();
        final Profile prof = new Profile.Fixed(
            new XMLDocument(
                new Joined(
                    "",
                    "<p><entry key='artifacts'>",
                    file.getAbsolutePath(),
                    "</entry></p>"
                ).asString()
            )
        );
        final Talk singtalk = ReleaseBinariesTest.talk(issue, tag, temp.toFile());
        new ReleaseBinaries(repo.github(), prof).execute(singtalk);
        MatcherAssert.assertThat(
            "Asset should be attached",
            new Releases.Smart(repo.releases()).find(tag).assets().get(0),
            Matchers.notNullValue()
        );
    }

    /**
     * Make a talk with this tag.
     * @param issue The issue
     * @param tag The tag
     * @param dir Daemon directory
     * @return Talk
     * @throws IOException If fails
     */
    private static Talk talk(
        final Issue issue,
        final String tag,
        final File dir
    ) throws IOException {
        final Talk talk = new Talk.InFile();
        final String identifier = "id";
        talk.modify(
            new Directives().xpath("/talk")
                .add("daemon").attr(identifier, "123")
                .add("title").set("merge").up()
                .add("script").set("empty").up()
                .add("dir").set(dir.getAbsolutePath()).up().up()
                .add("wire")
                .add("href").set("http://test2").up()
                .add("github-repo").set(issue.repo().coordinates().toString())
                .up()
                .add("github-issue").set(Integer.toString(issue.number())).up()
                .up()
                .add("request").attr(identifier, "abcdef")
                .add("type").set("release").up()
                .add("success").set(Boolean.TRUE.toString()).up()
                .add("args").add("arg").attr("name", "tag").set(tag)
        );
        return talk;
    }
}
