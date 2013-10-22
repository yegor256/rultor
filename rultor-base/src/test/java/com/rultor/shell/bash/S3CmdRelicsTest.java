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
import com.rultor.shell.ShellMocker;
import com.rultor.shell.Shells;
import com.rultor.spi.Coordinates;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link S3CmdRelics}.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
public final class S3CmdRelicsTest {

    /**
     * Skips upload when file not found.
     * @throws Exception If some problem inside
     */
    @Test
    public void skipsWhenFileNotFound() throws Exception {
        final File dir = Files.createTempDir();
        FileUtils.write(new File(dir, "s3cmd"), "echo $@ > cmd");
        final Shells shells = Mockito.mock(Shells.class);
        Mockito.when(shells.acquire()).thenReturn(new ShellMocker.Bash(dir));
        new S3CmdRelics(
            new Coordinates.Simple(),
            new ImmutableMap.Builder<String, String>()
                .put("log", "./log.txt").build(),
            "bucket-1", "",
            "AAAAAAAAAAAAAAAAAAEE",
            "30KFuodpOPX07QIaO4+QoLdTR5/MW/FN5qUDqxsL"
        ).exec(
            new Provisioned("PATH=.:$PATH && chmod +x s3cmd", shells).acquire()
        );
    }

}
