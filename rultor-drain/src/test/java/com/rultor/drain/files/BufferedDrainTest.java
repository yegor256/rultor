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
package com.rultor.drain.files;

import com.google.common.io.Files;
import com.jcabi.aspects.Tv;
import com.rultor.spi.Drain;
import java.io.File;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link BufferedDrain}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class BufferedDrainTest {

    /**
     * BufferedDrain can log and buffer.
     * @throws Exception If some problem inside
     */
    @Test
    public void logsAndBuffers() throws Exception {
        final File dir = Files.createTempDir();
        final Drain origin = Mockito.mock(Drain.class);
        Mockito.doReturn(IOUtils.toInputStream(""))
            .when(origin).read(Mockito.anyLong());
        final Drain drain = new BufferedDrain(
            TimeUnit.SECONDS.toMillis(1),
            TimeUnit.SECONDS.toMillis(Tv.FIVE),
            new File(dir, "a/b/c"),
            origin
        );
        final long date = new Random().nextLong();
        final String first = "some \t\u20ac\tfdsfs";
        final String second = "somefffffds900-4932%^&$%^&#%@^&!\u20ac\tfdsfs";
        MatcherAssert.assertThat(
            IOUtils.toString(drain.read(date), CharEncoding.UTF_8),
            Matchers.equalTo("")
        );
        drain.append(date, Arrays.asList(first, second));
        MatcherAssert.assertThat(
            IOUtils.toString(drain.read(date), CharEncoding.UTF_8),
            Matchers.containsString(first)
        );
    }

}
