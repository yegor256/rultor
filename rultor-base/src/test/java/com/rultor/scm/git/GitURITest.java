/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.scm.git;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test case for {@link GitURI}.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
@RunWith(Parameterized.class)
public final class GitURITest {

    /**
     * Git url to run test with.
     */
    private final transient String url;

    /**
     * Expected validity of url.
     */
    private final transient boolean isValid;

    /**
     * Public ctor.
     * @param giturl Address to check
     * @param expect Expected validity
     */
    public GitURITest(final String giturl, final boolean expect) {
        this.url = giturl;
        this.isValid = expect;
    }

    /**
     * Creates instance of GitURI when given GIT URL is valid.
     */
    @Test
    public void succeedsWhenGitUrlIsValid() {
        if (this.isValid) {
            new GitURI(this.url);
        }
    }

    /**
     * Fails when passed invalid GIT URL.
     */
    @Test(expected = IllegalArgumentException.class)
    public void failsWhenPassedInvalidGitURL() {
        if (this.isValid) {
            throw new IllegalArgumentException();
        }
        new GitURI(this.url);
    }

    /**
     * Test cases.
     * @return List of pairs of git url and isValid validity.
     */
    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(
            new Object[][]{
                {"ssh://host.xz/path/to/rep-o.git/", true},
                {"ssh://host.xz/path/to/rep-o.git", true},
                {"ssh://host.xz/path/to.some/rep-o.git", true},
                {"ssh://user@host.xz/path/to/repo.git/", true},
                {"ssh://user@host.xz:81/path/to/repo.git", true},
                {"ssh://host.xz:81/path/to/repo.git/", true},
                {"git://host.xz/path/to/repo.git/", true},
                {"git://host.xz:8383/path/to/repo.git/", true},
                {"http://host.xz/path/to/repo.git/", true},
                {"https://host.xz/path/to/repo.git/", true},
                {"http://host.xz:8181/path/to/repo.git/", true},
                {"ftp://host.xz/path/to/repo.git/", true},
                {"ftps://host.xz/path/to/repo.git/", true},
                {"ftp://host.xz:8181/path/to/repo.git/", true},
                {"rsync://host.xz/path/to/repo.git/", true},
                {"host.xz:path/to/repo.git/", true},
                {"user@host.xz:path/to/repo.git/", true},
                {"git@github.com:rultor/rultor.git", true},
                {"/path/to/repo.git/", true},
                {"file:///path/to/repo.git/", true},
                {"ssh1://host.xz/path/to/repo.git/", false},
                {"ssh://host.xz/path/to/repo.gi/", false},
                {"ssh:/host.xz/path/to/rep-o.git/", false},
                {"ssh://host.xz/path/to//", false},
                {"ssh://host.xz.git", false},
                {"ssh://@host.xz/path/to/repo.git/", false},
                {"ssh://user@host.xz:/path/to/repo.git/", false},
                {"ssh://host.xz:81/path/to/repo/", false},
                {"ssh://host.xz:8d1/path/to/repo/repo.git", false},
                {"ssh://host.xz:/path/to/repo/repo.git", false},
                {"git://host.xz/path/to/repo.gi/", false},
                {"git//host.xz/path/to/repo.git/", false},
                {"git:/host.xz/path/to/repo.git/", false},
                {"git://host.xz:8a383/path/to/repo.git/", false},
                {"git://host.xz:/path/to/repo.gi/", false},
                {"git://:8383/path/to/repo.git/", false},
                {"gits://host.xz:8383/path/to/repo.git/", false},
                {"http://host.xz/path/to/repo./", false},
                {"https://host.xz:/path/to/repo.git/", false},
                {"http://host.xz:81b81/path/to/repo.git/", false},
                {"ftp://host.xz/path/to/repo./", false},
                {"ftps://host.xz:/path/to/repo.git/", false},
                {"ftp://host.xz:81b81/path/to/repo.git/", false},
                {"rsync:/host.xz/path/to/repo.git/", false},
                {"rsync//host.xz/path/to/repo.git/", false},
                {":path/to/repo.git/", false},
                {"@host.xz:path/to/repo.git/", false},
                {"//path/to/repo.git/", false},
                {"file://path/to/repo.git/", false},
            }
        );
    }
}
