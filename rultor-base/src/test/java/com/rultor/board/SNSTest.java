/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.board;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.rultor.aws.SNSClient;
import java.io.IOException;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link com.rultor.board.SNS}.
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 */
public final class SNSTest {

    /**
     * SNS can publish on Amazon.
     * @throws IOException if some problem inside
     */
    @Test
    public void publishesOnAmazon() throws IOException {
        final SNSClient client = Mockito.mock(SNSClient.class);
        final AmazonSNS aws = Mockito.mock(AmazonSNS.class);
        Mockito.when(client.get()).thenReturn(aws);
        final Bill bill = Mockito.mock(Bill.class);
        final String body = "body1";
        final String subject = "subject1";
        Mockito.when(bill.body()).thenReturn(body);
        Mockito.when(bill.subject()).thenReturn(subject);
        final String topic = "topic1";
        final Billboard sns = new SNS(topic, client, bill);
        sns.announce(true);
        Mockito.verify(aws).publish(
            Mockito.argThat(
                Matchers.allOf(
                    Matchers.<PublishRequest>hasProperty(
                        "topicArn",
                        Matchers.equalTo(topic)
                    ),
                    Matchers.<PublishRequest>hasProperty(
                        "message",
                        Matchers.equalTo(body)
                    ),
                    Matchers.<PublishRequest>hasProperty(
                        "subject",
                        Matchers.equalTo(subject)
                    )
                )
            )
        );
        Mockito.verify(aws).shutdown();
    }

}
