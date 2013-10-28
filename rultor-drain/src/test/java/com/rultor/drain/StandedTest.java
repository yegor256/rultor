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
package com.rultor.drain;

import com.google.common.util.concurrent.MoreExecutors;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Timeable;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rexsl.test.TestClient;
import com.rexsl.test.TestResponse;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Drain;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.xembly.Directives;

/**
 * Tests for {@link Standed}.
 *
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class StandedTest {

    /**
     * Send of a single message.
     * @throws IOException In case of error
     */
    @Test
    public void appendingSingleMessage() throws IOException {
        final TestClient client = this.client();
        this.standed(client).append(this.xemblies(1));
        Mockito.verify(client).post(
            Mockito.anyString(),
            Mockito.argThat(
                Matchers.allOf(
                    this.matcher(1),
                    Matchers.not(
                        this.matcher(2)
                    )
                )
            )
        );
    }

    /**
     * Send of max batch size of message.
     * @throws IOException In case of error
     */
    @Test
    public void batchOfMessages() throws IOException {
        final TestClient client = this.client();
        // @checkstyle MagicNumberCheck (1 lines)
        this.standed(client).append(this.xemblies(10));
        Mockito.verify(client, Mockito.times(1))
            .post(Mockito.anyString(), Mockito.anyObject());
        Mockito.verify(client).post(
            Mockito.anyString(),
            Mockito.argThat(
                Matchers.allOf(
                    // @checkstyle MagicNumberCheck (2 lines)
                    this.matcher(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                    Matchers.not(this.matcher(11))
                )
            )
        );
    }

    /**
     * Send of two batches of message.
     * @throws IOException In case of error
     */
    @Test
    public void twoBatchesOfMessages() throws IOException {
        final TestClient client = this.client();
        // @checkstyle MagicNumberCheck (1 lines)
        this.standed(client).append(this.xemblies(11));
        Mockito.verify(client, Mockito.times(2))
            .post(Mockito.anyString(), Mockito.anyObject());
        Mockito.verify(client).post(
            Mockito.anyString(),
            Mockito.argThat(
                Matchers.allOf(
                    // @checkstyle MagicNumberCheck (2 lines)
                    this.matcher(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
                    Matchers.not(this.matcher(11))
                )
            )
        );
        Mockito.verify(client).post(
            Mockito.anyString(),
            Mockito.argThat(
                Matchers.allOf(
                    this.matcher(1),
                    Matchers.not(this.matcher(2))
                )
            )
        );
    }

    /**
     * Standed can make HTTP errors visible in logs (run this test
     * individually and read logs, you should see the message there
     * with WARN or higher priority).
     * @throws IOException In case of error
     */
    @Test
    public void httpSendingErrorsAreVisibleInLog() throws IOException {
        final TestClient client = Mockito.mock(TestClient.class);
        Mockito.doThrow(new IllegalStateException("failure!")).when(client)
            .header(Mockito.anyString(), Mockito.anyString());
        new Callable<Void>() {
            @Override
            @Timeable(limit = Tv.FIVE, unit = TimeUnit.SECONDS)
            @Loggable(Loggable.DEBUG)
            public Void call() throws IOException {
                StandedTest.this.standed(client).append(
                    StandedTest.this.xemblies(1)
                );
                return null;
            }
        } .call();
    }

    /**
     * Create instance of standed.
     * @param client TestClient to use
     * @return Standed instance
     */
    private Standed standed(final TestClient client) {
        return new Standed(
            new Coordinates.Simple(new URN(), "simple-rule", new Time()),
            "name", "pass",
            Mockito.mock(Drain.class),
            client,
            MoreExecutors.sameThreadExecutor()
        );
    }

    /**
     * Generate matcher for messages with identifiers.
     * @param identifiers Identifiers of a message.
     * @return Matcher for messages
     */
    @SuppressWarnings("unchecked")
    private Matcher<String> matcher(final int... identifiers) {
        final List<Matcher<String>> matchers =
            new ArrayList<Matcher<String>>(identifiers.length);
        for (final int identifier : identifiers) {
            matchers.add(
                Matchers.allOf(
                    Matchers.containsString(
                        String.format(
                            "SendMessageBatchRequestEntry.%1$d.Id=%1$d",
                            identifier
                        )
                    ),
                    Matchers.containsString(
                        String.format(
                            "SendMessageBatchRequestEntry.%1$d.MessageBody=",
                            identifier
                        )
                    )
                )
            );
        }
        return Matchers.allOf((Iterable) matchers);
    }

    /**
     * Create mock TestClient.
     * @return Mocked TestClient
     */
    private TestClient client() {
        final TestClient client = Mockito.mock(TestClient.class);
        final TestResponse response = Mockito.mock(TestResponse.class);
        Mockito.when(client.header(Mockito.anyString(), Mockito.anyObject()))
            .thenReturn(client);
        Mockito.when(client.post(Mockito.anyString(), Mockito.anyObject()))
            .thenReturn(response);
        Mockito.when(response.assertStatus(Mockito.anyInt()))
            .thenReturn(response);
        Mockito.when(response.xpath(Mockito.anyString()))
            .thenReturn(Collections.<String>emptyList());
        return client;
    }

    /**
     * Create a list of xemblies.
     * @param count Number of xemblies to create
     * @return Iterable with xemblies created
     */
    private Iterable<String> xemblies(final int count) {
        final Collection<String> xemblies = new ArrayList<String>(0);
        for (int idx = 0; idx < count; ++idx) {
            xemblies.add(this.xembly());
        }
        return xemblies;
    }

    /**
     * Simple xembly.
     * @return Xembly created.
     */
    private String xembly() {
        return new XemblyLine(new Directives().set("line")).toString();
    }
}
