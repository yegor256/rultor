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
package com.rultor.log4j;

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.rultor.spi.Drain;
import com.rultor.tools.Time;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link GroupAppender}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class GroupAppenderTest {

    /**
     * GroupAppender can log slf4j messages.
     * @throws Exception If some problem inside
     */
    @Test
    public void logsMessages() throws Exception {
        final Drain drain = Mockito.mock(Drain.class);
        final GroupAppender appender =
            new GroupAppender(new Time(), drain);
        appender.setLayout(new PatternLayout("%m"));
        final String text = "test message to see in log";
        final Thread publisher = new Thread() {
            @Override
            public void run() {
                appender.append(
                    new LoggingEvent(
                        "",
                        Logger.getLogger(this.getClass()),
                        org.apache.log4j.Level.INFO,
                        text,
                        new IllegalArgumentException()
                    )
                );
            }
        };
        publisher.start();
        publisher.join();
        appender.close();
        Mockito.verify(drain).append(
            Mockito.argThat(
                Matchers.everyItem(
                    Matchers.endsWith(
                        " INFO test message to see in log"
                    )
                )
            )
        );
    }

    /**
     * GroupAppender can log slf4j messages in the same thread.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("unchecked")
    public void logsMessagesInSameThread() throws Exception {
        final Drain drain = Mockito.mock(Drain.class);
        final GroupAppender appender =
            new GroupAppender(new Time(), drain);
        appender.setLayout(new PatternLayout(" %m"));
        Logger.getRootLogger().addAppender(appender);
        appender.append(
            new LoggingEvent(
                "",
                Logger.getLogger(this.getClass()),
                org.apache.log4j.Level.INFO,
                "some text to log",
                new IllegalArgumentException()
            )
        );
        Logger.getRootLogger().removeAppender(appender);
        appender.close();
        Mockito.verify(drain).append(
            (Iterable<String>) Mockito.any(Object.class)
        );
    }

    /**
     * GroupAppender can log in multiple threads.
     * @throws Exception If some problem inside
     */
    @Test
    public void logsMessagesInMultipleThreads() throws Exception {
        final String text = "test message to see \u20ac";
        final CountDownLatch start = new CountDownLatch(1);
        final int threads = Tv.TEN;
        final CountDownLatch done = new CountDownLatch(threads);
        final CountDownLatch ready = new CountDownLatch(threads);
        final Collection<GroupAppender> appenders =
            new CopyOnWriteArrayList<GroupAppender>();
        final Runnable runnable = new VerboseRunnable(
            // @checkstyle AnonInnerLength (50 lines)
            new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    final Drain drain = Mockito.mock(Drain.class);
                    final GroupAppender appender =
                        new GroupAppender(new Time(), drain);
                    appender.setLayout(new PatternLayout("%m%n"));
                    appenders.add(appender);
                    ready.countDown();
                    assert start.await(1, TimeUnit.SECONDS);
                    final LoggingEvent event = new LoggingEvent(
                        "",
                        Logger.getLogger(this.getClass()),
                        org.apache.log4j.Level.INFO,
                        text,
                        new IllegalArgumentException()
                    );
                    for (GroupAppender app : appenders) {
                        app.append(event);
                        app.run();
                    }
                    Mockito.verify(drain).append(
                        Mockito.argThat(Matchers.<String>iterableWithSize(1))
                    );
                    done.countDown();
                    return null;
                }
            }
        );
        final ExecutorService svc = Executors.newFixedThreadPool(
            threads,
            new ThreadFactory() {
                private final transient AtomicLong group = new AtomicLong();
                @Override
                public Thread newThread(final Runnable runnable) {
                    return new Thread(
                        new ThreadGroup(
                            Long.toString(this.group.incrementAndGet())
                        ),
                        runnable,
                        String.format("app-%d", this.group.get())
                    );
                }
            }
        );
        for (int thread = 0; thread < threads; ++thread) {
            svc.submit(runnable);
        }
        MatcherAssert.assertThat(
            ready.await(1, TimeUnit.SECONDS),
            Matchers.is(true)
        );
        start.countDown();
        MatcherAssert.assertThat(
            done.await(1, TimeUnit.SECONDS),
            Matchers.is(true)
        );
    }

}
