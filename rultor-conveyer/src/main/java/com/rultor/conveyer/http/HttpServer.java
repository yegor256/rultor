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
package com.rultor.conveyer.http;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Http server in front of streams.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@Loggable(Loggable.INFO)
@EqualsAndHashCode(of = { "frontend", "backend", "sockets", "server" })
@SuppressWarnings("PMD.DoNotUseThreads")
public final class HttpServer implements Closeable {

    /**
     * How many threads to use.
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * 8;

    /**
     * Executor service, with socket openers.
     */
    private final transient ScheduledExecutorService frontend =
        Executors.newSingleThreadScheduledExecutor(new VerboseThreads("front"));

    /**
     * Executor service, with consuming threads.
     */
    private final transient ScheduledExecutorService backend =
        Executors.newScheduledThreadPool(
            HttpServer.THREADS,
            new VerboseThreads("back")
        );

    /**
     * Blocking queue of ready-to-be-processed sockets.
     */
    private final transient BlockingQueue<Socket> sockets =
        new SynchronousQueue<Socket>();

    /**
     * Server socket.
     */
    private final transient ServerSocket server;

    /**
     * Public ctor.
     * @param streams Streams
     * @param port Port we're at
     * @throws IOException If fails
     */
    public HttpServer(final Streams streams, final int port)
        throws IOException {
        this.server = new ServerSocket(port);
        final HttpThread thread = new HttpThread(this.sockets, streams);
        final Runnable runnable = new VerboseRunnable(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        thread.dispatch();
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            },
            true, false
        );
        for (int idx = 0; idx < HttpServer.THREADS; ++idx) {
            this.backend.scheduleWithFixedDelay(
                runnable,
                TimeUnit.SECONDS.toNanos(Tv.FIVE), 1L, TimeUnit.NANOSECONDS
            );
        }
        Logger.info(
            HttpServer.class, "HTTP srv scheduled on TCP port %d, %d thread(s)",
            port, HttpServer.THREADS
        );
    }

    /**
     * Start listening to the port.
     */
    public void listen() {
        this.frontend.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpServer.this.process();
                        } catch (final InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                },
                true, false
            ),
            0, 1, TimeUnit.NANOSECONDS
        );
    }

    @Override
    public void close() throws IOException {
        this.server.close();
        this.shutdown(this.frontend);
        this.shutdown(this.backend);
    }

    /**
     * Process one socket.
     * @throws InterruptedException When thread is interrupted.
     */
    private void process() throws InterruptedException {
        Socket socket;
        try {
            socket = this.server.accept();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            final boolean consumed = this.sockets
                .offer(socket, 1L, TimeUnit.SECONDS);
            if (!consumed) {
                socket.close();
                throw new IOException("too many sockets");
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Shutdown a service.
     * @param service The service to shut down
     */
    private void shutdown(final ScheduledExecutorService service) {
        service.shutdown();
        try {
            if (service.awaitTermination(1L, TimeUnit.SECONDS)) {
                Logger.info(this, "#shutdown(): succeeded");
            } else {
                Logger.warn(this, "#shutdown(): failed");
                service.shutdownNow();
                if (service.awaitTermination(1L, TimeUnit.SECONDS)) {
                    Logger.info(this, "#shutdown(): shutdownNow() succeeded");
                } else {
                    Logger.error(this, "#shutdown(): failed to stop threads");
                }
            }
        } catch (final InterruptedException ex) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
