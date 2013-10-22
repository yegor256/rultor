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

import com.google.common.io.Files;
import com.rultor.shell.ShellMocker;
import com.rultor.shell.Shells;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link com.rultor.shell.bash.S3CmdPut}.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 */
public final class S3CmdPutTest {

    /**
     * Puts files to s3.
     * @throws Exception If some problem inside
     */
    @Test
    public void executesUploadCmd() throws Exception {
        final File dir = Files.createTempDir();
        FileUtils.write(new File(dir, "s3cmd"), "echo $@");
        final S3CmdPut cmd = new S3CmdPut(
            "a1", "./*", "bkt1", "",
            "AAAAAAAAAAAAAAAAAAAA",
            "30KFuodpOPX07QIaO4+QoLdTR5/MW/FN5qUDqxs="
        );
        final Shells shells = Mockito.mock(Shells.class);
        Mockito.when(shells.acquire()).thenReturn(new ShellMocker.Bash(dir));
        cmd.exec(
            new Provisioned("PATH=.:$PATH && chmod +x s3cmd", shells).acquire()
        );
    }

    /**
     * Parametrizes s3cmd with type.
     * @throws Exception If some problem inside
     */
    @Test
    public void parametrizesCmdWithType() throws Exception {
        final File dir = Files.createTempDir();
        FileUtils.write(new File(dir, "s3cmd"), "cat ${1#*=} > stdout");
        final S3CmdPut cmd = new S3CmdPut(
            "a2", new File(dir, "*").getAbsolutePath(), "bkt2", "",
            "AAAAAAAAAAAAAAAAAAEE",
            "30KFuodpOPX07QIaO4+QoLdTR5/MW/FN5qUDqxsL",
            "text/html", "utf-8"
        );
        final Shells shells = Mockito.mock(Shells.class);
        Mockito.when(shells.acquire()).thenReturn(new ShellMocker.Bash(dir));
        cmd.exec(
            new Provisioned("PATH=.:$PATH && chmod +x s3cmd", shells).acquire()
        );
        MatcherAssert.assertThat(
            FileUtils.readFileToString(new File(dir, "stdout")),
            Matchers.containsString("mime-type=text/html")
        );
    }

    /**
     * Parametrizes s3cmd with encoding.
     * @throws Exception If some problem inside
     */
    @Test
    public void parametrizesCmdWithEncoding() throws Exception {
        final File dir = Files.createTempDir();
        FileUtils.write(new File(dir, "s3cmd"), "cat ${1#*=} > stdout");
        FileUtils.write(new File(dir, "something.txt"), "");
        final S3CmdPut cmd = new S3CmdPut(
            "a3", new File(dir, "something.txt").getAbsolutePath(), "bkt3", "",
            "AAAAAAAAAAAAAAAAAAFF",
            "cQTLve84UnNzYyo848o1oVkIX7RhOFimeQoM7vJl",
            "text/plain", "win-1252"
        );
        final Shells shells = Mockito.mock(Shells.class);
        Mockito.when(shells.acquire()).thenReturn(new ShellMocker.Bash(dir));
        cmd.exec(
            new Provisioned("PATH=.:$PATH && chmod +x s3cmd", shells).acquire()
        );
        MatcherAssert.assertThat(
            FileUtils.readFileToString(new File(dir, "stdout")),
            Matchers.containsString("encoding=win-1252")
        );
    }

}
