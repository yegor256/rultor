/**
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.agents.github;

import com.jcabi.aspects.Tv;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.cactoos.io.TeeInput;
import org.cactoos.scalar.LengthOf;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xembly.Directives;

/**
 * Tests for {@link ReleaseBinaries}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (200 lines)
 */
final class ReleaseBinariesTest {

    /**
     * ReleaseBinaries should attach artifact to release.
     * @param temp Temporary folder for talk
     * @throws Exception In case of error
     */
    @Test
    @Disabled
    public void attachesBinaryToRelease(
        @TempDir final Path temp
    ) throws Exception {
        final Repo repo = new MkGithub().randomRepo();
        final String tag = "v1.0";
        final File dir = temp.toFile();
        final String target = "target";
        final String name = "name-${tag}.jar";
        final File bin = FileUtils.getFile(
            dir.getAbsolutePath(), "repo", target, name.replace("${tag}", tag)
        );
        bin.mkdirs();
        new SecureRandom();
        final byte[] content = SecureRandom.getSeed(Tv.HUNDRED);
        new LengthOf(new TeeInput(content, bin)).value();
        final Talk talk = ReleaseBinariesTest
            .talk(repo.issues().create("", ""), tag, dir);
        new CommentsTag(repo.github()).execute(talk);
        new ReleaseBinaries(
            repo.github(),
            new Profile.Fixed(
                new XMLDocument(
                    StringUtils.join(
                        "<p><entry key='release'><entry key='artifacts'>",
                        String.format("%s/%s", target, name),
                        "</entry></entry></p>"
                    )
                )
            )
        ).execute(talk);
        MatcherAssert.assertThat(
            IOUtils.toByteArray(
                new Releases.Smart(repo.releases()).find(tag)
                    .assets().get(0).raw()
            ),
            Matchers.equalTo(content)
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
