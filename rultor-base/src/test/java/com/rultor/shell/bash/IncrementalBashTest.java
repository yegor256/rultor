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
package com.rultor.shell.bash;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.rexsl.test.XhtmlMatchers;
import com.rultor.shell.Permanent;
import com.rultor.shell.ShellMocker;
import com.rultor.snapshot.Snapshot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link IncrementalBash}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class IncrementalBashTest {

    /**
     * IncrementalBash can run complex script with bash.
     * @throws Exception If some problem inside
     */
    @Test
    public void runsIncrementalBashScript() throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final File dir = Files.createTempDir();
        FileUtils.write(new File(dir, "a.txt"), "first\nsecond");
        final ImmutableMap<String, Object> args =
            new ImmutableMap.Builder<String, Object>()
                .put("file", "file-name.txt")
                .build();
        final int code = new IncrementalBash(
            new Permanent(new ShellMocker.Bash(dir)),
            Arrays.asList(
                "MSG='$A'; echo `date` $A; sleep 1; pwd;",
                "find . -name \"a.txt\" | grep txt | wc -l;",
                "mkdir -p foo; cd foo; touch ${file}; pwd",
                "pwd; if [ ! -f ${file.toString()} ]; then exit 1; fi",
                "/usr/bin/--broken-name; /usr/bin/--again"
            )
        ).exec(args, stdout);
        MatcherAssert.assertThat(code, Matchers.not(Matchers.equalTo(0)));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Snapshot(
                    new ByteArrayInputStream(stdout.toByteArray())
                ).dom()
            ),
            XhtmlMatchers.hasXPaths(
                "/snapshot/steps/step",
                // @checkstyle LineLength (5 lines)
                "//step[summary=\"MSG='$A'; echo `date` $A; sleep 1; pwd;\"]/start",
                "//step[summary='/usr/bin/--broken-name; /usr/bin/--again']/exception",
                "//step/exception[contains(stacktrace,'/usr/bin/--broken-name: No such file or directory')]",
                "//steps[count(step[level='INFO']) = 4]",
                "//steps[count(step[level='SEVERE']) = 1]",
                "//steps[count(step[start]) = 5]",
                "//steps[count(step[finish]) = 5]",
                "//steps[count(step[duration = '']) = 0]"
            )
        );
    }

}
