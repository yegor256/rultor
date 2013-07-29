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

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.rexsl.test.RestTester;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link HttpServer}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class HttpServerTest {

    /**
     * HttpServer can process parallel requests.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void handlesParallelHttpRequests() throws Exception {
        final int port = this.reserve();
        final URI uri = URI.create(
            String.format("http://localhost:%d/", port)
        );
        final Streams streams = Mockito.mock(Streams.class);
        Mockito.doAnswer(
            new Answer<InputStream>() {
                @Override
                public InputStream answer(final InvocationOnMock inv) {
                    return IOUtils.toInputStream("one\ntwo\nthree");
                }
            }
        ).when(streams).stream("abc");
        final HttpServer server = new HttpServer(streams, port);
        server.listen();
        final int threads = Tv.FIFTY;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(threads);
        final URI path = UriBuilder.fromUri(uri).path("/abc").build();
        final Callable<?> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                start.await();
                RestTester.start(path)
                    .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                    .get("read sample stream")
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(Matchers.startsWith("one\n"));
                finished.countDown();
                return null;
            }
        };
        final ExecutorService svc =
            Executors.newFixedThreadPool(threads, new VerboseThreads());
        for (int thread = 0; thread < threads; ++thread) {
            svc.submit(new VerboseRunnable(task, true));
        }
        start.countDown();
        MatcherAssert.assertThat(
            finished.await(1, TimeUnit.MINUTES),
            Matchers.is(true)
        );
        svc.shutdown();
        server.close();
    }

    /**
     * Find and return the first available port.
     * @return The port number
     * @throws IOException If fails
     */
    private int reserve() throws IOException {
        int port;
        final ServerSocket socket = new ServerSocket(0);
        try {
            port = socket.getLocalPort();
        } finally {
            socket.close();
        }
        return port;
    }

}
