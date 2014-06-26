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
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.shell.Permanent;
import com.rultor.shell.ShellMocker;
import com.rultor.snapshot.Snapshot;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Concat}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class ConcatTest {

    /**
     * Concat can run complex script with bash.
     * @throws Exception If some problem inside
     */
    @Test
    public void runsIncrementalBashScript() throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final File dir = Files.createTempDir();
        FileUtils.write(new File(dir, "a.txt"), "first\nsecond");
        final ImmutableMap<String, String> args =
            new ImmutableMap.Builder<String, String>()
                .put("file", "file-name.txt")
                .build();
        final int code = new Bash(
            new Permanent(new ShellMocker.Bash(dir)),
            new Concat(
                Arrays.asList(
                    "MSG='$A'; echo $(date) $A; sleep 1; pwd;",
                    "find . -name \"a.txt\" | grep txt | wc -l;",
                    "mkdir -p foo; cd foo; touch ${file}; pwd",
                    "pwd; if [ ! -f ${file.toString()} ]; then exit 1; fi",
                    "echo -e \"\\x1b\\x09\\x9B\" >&2; /usr/--bad; /usr/--again"
                )
            ).object()
        ).exec(args, stdout);
        MatcherAssert.assertThat(code, Matchers.not(Matchers.equalTo(0)));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Snapshot(
                    new ByteArrayInputStream(stdout.toByteArray())
                ).xml()
            ),
            XhtmlMatchers.hasXPaths(
                "/snapshot/steps/step",
                // @checkstyle LineLength (5 lines)
                "//step[summary=\"MSG='$A'; echo $(date) $A; sleep 1; pwd;\"]/start",
                "//step[contains(summary, '/usr/--bad; /usr/--again')]/exception",
                "//step/exception[contains(stacktrace,'/usr/--bad: No such file or directory')]",
                "//steps[count(step[level='INFO']) = 4]",
                "//steps[count(step[level='SEVERE']) = 1]",
                "//steps[count(step[start]) = 5]",
                "//steps[count(step[finish]) = 5]",
                "//steps[count(step[duration = '']) = 0]"
            )
        );
    }

    /**
     * Concat can escape command summary as Markdown.
     * @throws Exception If some problem inside
     */
    @Test
    public void escapesMarkdownInCommandSummary() throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final File dir = Files.createTempDir();
        final int code = new Bash(
            new Permanent(new ShellMocker.Bash(dir)),
            new Concat(Arrays.asList("echo \"_*\" `date`")).object()
        ).exec(new ImmutableMap.Builder<String, String>().build(), stdout);
        MatcherAssert.assertThat(code, Matchers.equalTo(0));
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Snapshot(
                    new ByteArrayInputStream(stdout.toByteArray())
                ).xml()
            ),
            XhtmlMatchers.hasXPath(
                "//step[summary='echo \"\\_\\*\" \\`date\\`']/start"
            )
        );
    }

    /**
     * Concat can show only HEAD and TAIL of stacktrace.
     * @throws Exception If some problem inside
     */
    @Test
    public void showsHeadAndTailOfStderr() throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final File dir = Files.createTempDir();
        new Bash(
            new Permanent(new ShellMocker.Bash(dir)),
            new Concat(
                Arrays.asList(
                    "( for i in {1..300}; do echo -$i-; done; exit 1 ) >&2"
                )
            ).object()
        ).exec(new ImmutableMap.Builder<String, String>().build(), stdout);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Snapshot(
                    new ByteArrayInputStream(stdout.toByteArray())
                ).xml()
            ),
            XhtmlMatchers.hasXPaths(
                "//level[.='SEVERE']",
                "//exception[contains(stacktrace, '-25-')]",
                "//exception[not(contains(stacktrace, '-26-'))]",
                // @checkstyle LineLength (1 lines)
                "//exception[contains(stacktrace, '... 175 lines skipped ...')]",
                "//exception[not(contains(stacktrace, '199'))]",
                "//exception[contains(stacktrace, '300')]"
            )
        );
    }

    /**
     * Concat can show full stacktrace.
     * @throws Exception If some problem inside
     */
    @Test
    public void showsStderrWithoutCuts() throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final File dir = Files.createTempDir();
        new Bash(
            new Permanent(new ShellMocker.Bash(dir)),
            new Concat(
                Arrays.asList(
                    "( for i in {1..75}; do echo -$i-; done; sync; exit 1 ) >&2"
                )
            ).object()
        ).exec(new ImmutableMap.Builder<String, String>().build(), stdout);
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new Snapshot(
                    new ByteArrayInputStream(stdout.toByteArray())
                ).xml()
            ),
            XhtmlMatchers.hasXPaths(
                "//exception[contains(stacktrace, '-24-')]",
                "//exception[not(contains(stacktrace, 'lines skipped'))]",
                "//exception[contains(stacktrace, '75')]"
            )
        );
    }

    /**
     * Concat can output stderr and stdout correctly.
     * @throws Exception If some problem inside
     */
    @Test
    public void correctlyPrintsStderrAndStdout() throws Exception {
        final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        final File dir = Files.createTempDir();
        new Bash(
            new Permanent(new ShellMocker.Bash(dir)),
            new Concat(
                Arrays.asList(
                    "echo -e 'one\\ntwo' >&2; echo -e 'foo-1\\nfoo-2'"
                )
            ).object()
        ).exec(new ImmutableMap.Builder<String, String>().build(), stdout);
        MatcherAssert.assertThat(
            new String(stdout.toByteArray(), CharEncoding.UTF_8),
            Matchers.allOf(
                Matchers.containsString("one\ntwo"),
                Matchers.containsString("foo-1\nfoo-2")
            )
        );
    }

}
