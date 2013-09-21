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

import com.google.common.io.Files;
import com.jcabi.aspects.Tv;
import com.rultor.scm.Commit;
import com.rultor.shell.Shell;
import com.rultor.shell.Terminal;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.validation.ConstraintViolationException;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link GitBranch}.
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @version $Id$
 */
public final class GitBranchTest {

    /**
     * GitBranch public ctor args can not be null.
     * @throws Exception If some problem inside
     */
    @Test(expected = ConstraintViolationException.class)
    public void failsWhenInitializedWithNulls() throws Exception {
        new GitBranch(null, null, null);
    }

    /**
     * GtiBranch limits output.
     * @throws Exception if some problem inside
     */
    @Test
    public void limitsCommitHistory() throws Exception {
        final Shell shl = Mockito.mock(Shell.class);
        final Terminal terminal = new Terminal(shl);
        final StringBuilder log = new StringBuilder();
        // @checkstyle LineLength (1 line)
        log.append("97d27074cc60ce0e470aa7dfbf78ffc8c53047b4 tarahbb@maunh.com 2013-09-12 23:12:11 +0200 typo in XSL\n")
            // @checkstyle LineLength (1 line)
            .append("1fdba5f135fbe4efe3980702c1bfaeef7b275078 tarahbb@maunh.com 2013-09-12 23:07:45 +0200 upgraded mongodb java driver to 2.1\n")
            // @checkstyle LineLength (1 line)
            .append("728b18a9e61718b63780b851af8f5614c403e1fe tarahbb@maunh.com 2013-09-12 22:39:49 +0200 regex fixed\n")
            // @checkstyle LineLength (1 line)
            .append("f6e6afaed65ee20cad46da009ef878b4b279aa87 tarahbb@maunh.com 2013-09-12 22:25:01 +0200 Merge branch 'master'\n")
            // @checkstyle LineLength (1 line)
            .append("2d651cf5d5e230de0bca2e6bad18f295718ff36f tarahbb@maunh.com 2013-09-12 22:24:57 +0200 summary without XML, just plain text\n")
            // @checkstyle LineLength (1 line)
            .append("0cb0711e02641b118de11fe802e245c18a562b2f tarahbb@maunh.com 2013-09-12 13:22:43 -0700 Merge pull request\n")
            // @checkstyle LineLength (1 line)
            .append("6ca2ecaf933f4d6860588d78dfed165a1bfdbbb5 tarahbb@maunh.com 2013-09-12 22:16:29 +0200 show open pulses on top of everything\n")
            // @checkstyle LineLength (1 line)
            .append("5d09a37569d7ecad2aa1d28a6e645cdac35f6f00 tarahbb@maunh.com 2013-09-12 22:03:37 +0200 Merge branch 'master'\n")
            // @checkstyle LineLength (1 line)
            .append("dc5725c7c3cd5323704db3745a04305fdeeca9d6 tarahbb@maunh.com 2013-09-12 13:01:22 -0700 Merge pull request\n")
            // @checkstyle LineLength (1 line)
            .append("34aecdb81397189c3869031aac4f3ca501d4b800 tarahbb@maunh.com 2013-09-13 03:00:59 +0700 Indentation improved");
        // @checkstyle LineLength (1 line)
        Mockito.doAnswer(this.answer(log.toString())).doAnswer(this.answer(""))
        // @checkstyle LineLength (1 line)
        .when(shl).exec(Mockito.anyString(), Mockito.any(InputStream.class), Mockito.any(OutputStream.class), Mockito.any(OutputStream.class));
        final Iterable<Commit> itr =  new GitBranch(
            terminal,
            Files.createTempDir().getAbsolutePath(),
            "test"
        ).log();
        MatcherAssert.assertThat(
            itr,
            Matchers.<Commit>iterableWithSize(Tv.TEN)
        );
    }

    /**
     * Creates new answer object.
     * @param log Log.
     * @return Answer
     */
    private Answer<Void> answer(final String log) {
        final InputStream reader = new ByteArrayInputStream(
            log.getBytes()
        );
        return new Answer<Void>() {
            // @checkstyle LineLength (1 line)
            public Void answer(final InvocationOnMock invocation) throws IOException {
                final Object[] args = invocation.getArguments();
                IOUtils.copy(
                    reader,
                    (OutputStream) args[2]
                );
                reader.close();
                return null;
            }
        };
    }
}
