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
package com.rultor.shell.bash;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.rultor.shell.Permanent;
import com.rultor.shell.ShellMocker;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Prerequisites}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 */
public final class PrerequisitesTest {

    /**
     * Prerequisites can prepare requisites for another script.
     * @throws Exception If some problem inside
     */
    @Test
    public void preloadsFiles() throws Exception {
        final File dir = Files.createTempDir();
        FileUtils.write(new File(dir, "a.txt"), "first\nsecond");
        new Prerequisites(
            new Permanent(new ShellMocker.Bash(dir)),
            new ImmutableMap.Builder<String, Object>()
                .put("./a/b/c/f.txt", "hello, друг!")
                .put(".foo/x.txt", "hi, друг!")
                .put("a.txt", "")
                .build()
        ).acquire();
        MatcherAssert.assertThat(
            new File(dir, ".foo/x.txt").exists(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new File(dir, "a.txt").exists(),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new File(dir, "a/b/c/f.txt").exists(),
            Matchers.is(true)
        );
    }

}
