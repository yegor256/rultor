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
package com.rultor.conveyer;

import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.CharEncoding;
import org.apache.log4j.PatternLayout;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Log4jStreams}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class Log4jStreamsTest {

    /**
     * Log4jStreams can write to buffer and read from it.
     * @throws Exception If some problem inside
     */
    @Test
    public void writesAndReads() throws Exception {
        final Log4jStreams streams = new Log4jStreams();
        streams.setLayout(new PatternLayout("%p %m\n"));
        final String key = streams.register();
        try {
            Logger.info(this, "first");
            Logger.info(this, "тест 55");
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final Thread thread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final InputStream input = streams.stream(key);
                            while (true) {
                                baos.write(input.read());
                            }
                        } catch (IOException ex) {
                            throw new IllegalStateException(ex);
                        }
                    }
                }
            );
            thread.start();
            thread.join(TimeUnit.SECONDS.toMillis(1));
            thread.interrupt();
            MatcherAssert.assertThat(
                baos.toString(CharEncoding.UTF_8),
                Matchers.endsWith("INFO first\nINFO тест 55\n")
            );
        } finally {
            streams.unregister(key);
        }
    }

}
