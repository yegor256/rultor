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
package com.rultor.board;

import javax.validation.ConstraintViolationException;
import org.junit.Test;
import org.mockito.Mockito;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.rultor.aws.SNSClient;

/**
 * Test case for {@link SNS}.
 *
 * @author Gangababu Tirumalanadhuni (gangababu.t@gmail.com)
 * @version $Id$
 */
public final class SNSTest {
    /**
     * SNS announce's body can not be null.
     *
     * @throws Exception
     * If some problem inside
     */
     @Test(expected = ConstraintViolationException.class)
     public void bodyCantBeNull() throws Exception {
       new SNS("arn", "key", "secret").announce(null);
     }

    /**
     * Send notification message to topic.
     */
    @Test
    public void sentNotification() {
        String body = "Subject \n message body";
        String topicArn = "Test Topic";
        String sub = "Test Subject";
        String strBody = "Test Body";

        final AmazonSNS aws = Mockito.mock(AmazonSNS.class);
        final SNSClient client = Mockito.mock(SNSClient.class);
        final PublishResult publishResult = Mockito.mock(PublishResult.class);
        PublishRequest publishRequest = Mockito.mock(PublishRequest.class);
        Mockito.doReturn(publishRequest).when(publishRequest)
               .withTopicArn(topicArn);
        Mockito.doReturn(publishRequest).when(publishRequest)
               .withMessage(strBody);
        Mockito.doReturn(publishRequest).when(publishRequest).withSubject(sub);
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(publishResult).when(aws).publish(publishRequest);

        new SNS(topicArn, client).announce(body);
      }
}
