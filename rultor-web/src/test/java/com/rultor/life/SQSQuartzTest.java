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
package com.rultor.life;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Queue;
import com.rultor.spi.Rule;
import com.rultor.spi.Rules;
import com.rultor.spi.Spec;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.util.Arrays;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link SQSQuartz}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SQSQuartzTest {

    /**
     * SQSQuartz can publish works into queue.
     * @throws Exception If some problem inside
     * @checkstyle ExecutableStatementCount (200 lines)
     */
    @Test
    public void publishesWorksIntoQueue() throws Exception {
        final Users users = Mockito.mock(Users.class);
        final User user = Mockito.mock(User.class);
        Mockito.doReturn(Arrays.asList(user).iterator())
            .when(users).iterator();
        Mockito.doReturn(new URN("urn:github:1")).when(user).urn();
        final Rule rule = Mockito.mock(Rule.class);
        Mockito.doReturn("some-rule").when(rule).name();
        final Rules rules = Mockito.mock(Rules.class);
        Mockito.doReturn(rules).when(user).rules();
        Mockito.doReturn(Arrays.asList(rule).iterator())
            .when(rules).iterator();
        Mockito.doReturn(new Spec.Simple()).when(rule).spec();
        final Queue queue = Mockito.mock(Queue.class);
        final SQSClient client = Mockito.mock(SQSClient.class);
        final AmazonSQS aws = Mockito.mock(AmazonSQS.class);
        final Time time = new Time("2013-07-21T13:36:00Z");
        final String handle = "some-random-text";
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(
            new ReceiveMessageResult().withMessages(
                new Message()
                    .withBody(time.toString())
                    .withReceiptHandle(handle)
            )
        ).when(aws).receiveMessage(Mockito.any(ReceiveMessageRequest.class));
        final SQSQuartz quartz = new SQSQuartz(users, queue, client);
        quartz.run();
        Mockito.verify(aws).receiveMessage(
            Mockito.any(ReceiveMessageRequest.class)
        );
        Mockito.verify(aws).sendMessage(
            Mockito.argThat(
                Matchers.<SendMessageRequest>hasProperty(
                    "messageBody",
                    Matchers.equalTo("2013-07-21T13:37:00Z")
                )
            )
        );
        Mockito.verify(aws).deleteMessage(
            Mockito.argThat(
                Matchers.<DeleteMessageRequest>hasProperty(
                    "receiptHandle",
                    Matchers.equalTo(handle)
                )
            )
        );
        Mockito.verify(queue).push(
            Mockito.argThat(
                new CustomMatcher<Work>("expected work") {
                    @Override
                    public boolean matches(final Object work) {
                        return Work.class.cast(work).scheduled().equals(time);
                    }
                }
            )
        );
    }

}
