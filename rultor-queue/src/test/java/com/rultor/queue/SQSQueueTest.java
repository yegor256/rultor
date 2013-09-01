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
package com.rultor.queue;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link SQSQueue}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SQSQueueTest {

    /**
     * SQSQueue can save JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void savesJson() throws Exception {
        final Work work = new Work.Simple(
            new URN("urn:facebook:1"),
            "some-test-rule",
            new Time(Tv.MILLION)
        );
        final SQSClient client = Mockito.mock(SQSClient.class);
        final AmazonSQS aws = Mockito.mock(AmazonSQS.class);
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(
            new SendMessageResult().withMessageId("test")
        ).when(aws).sendMessage(Mockito.any(SendMessageRequest.class));
        Mockito.doReturn(new GetQueueAttributesResult())
            .when(aws)
            .getQueueAttributes(Mockito.any(GetQueueAttributesRequest.class));
        final SQSQueue queue = new SQSQueue(client);
        queue.push(work);
        Mockito.verify(aws).sendMessage(
            Mockito.argThat(
                Matchers.<SendMessageRequest>hasProperty(
                    "messageBody",
                    Matchers.equalTo(
                        // @checkstyle LineLength (1 line)
                        "{\"urn\":\"urn:facebook:1\",\"scheduled\":\"1970-01-01T00:16:40Z\",\"rule\":\"some-test-rule\"}"
                    )
                )
            )
        );
    }

    /**
     * SQSQueue can restore JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void restoresJson() throws Exception {
        final SQSClient client = Mockito.mock(SQSClient.class);
        final AmazonSQS aws = Mockito.mock(AmazonSQS.class);
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(new GetQueueAttributesResult())
            .when(aws)
            .getQueueAttributes(Mockito.any(GetQueueAttributesRequest.class));
        final SQSQueue queue = new SQSQueue(client);
        Mockito.doReturn(
            new ReceiveMessageResult().withMessages(
                new Message().withBody(
                    // @checkstyle LineLength (1 line)
                    "{\"urn\":\"urn:facebook:65\",\"rule\":\"test-877\",\"scheduled\":\"1970-01-01T00:16:40Z\"}"
                )
            )
        ).when(aws).receiveMessage(Mockito.any(ReceiveMessageRequest.class));
        final Work work = queue.pull(1, TimeUnit.SECONDS);
        MatcherAssert.assertThat(
            work.owner(), Matchers.equalTo(new URN("urn:facebook:65"))
        );
        MatcherAssert.assertThat(work.rule(), Matchers.equalTo("test-877"));
        MatcherAssert.assertThat(
            work.scheduled().toString(),
            Matchers.equalTo(new Time(Tv.MILLION).toString())
        );
    }

}
