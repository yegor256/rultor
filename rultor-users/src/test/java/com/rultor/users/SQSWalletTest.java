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
package com.rultor.users;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonObject;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link SQSWallet}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class SQSWalletTest {

    /**
     * SQSWallet can send JSON to AWS.
     * @throws Exception If some problem inside
     */
    @Test
    public void sendsJsonToAws() throws Exception {
        final SQSClient client = Mockito.mock(SQSClient.class);
        final AmazonSQS aws = Mockito.mock(AmazonSQS.class);
        Mockito.doReturn(aws).when(client).get();
        final Wallet wallet = new SQSWallet(
            client, new Work.Simple(),
            new URN("urn:creditor:1"), "credit-rule",
            new URN("urn:debitor:1"), "debit-rule"
        );
        wallet.charge("payment детали", new Dollars(Tv.MILLION));
        Mockito.verify(aws).sendMessage(
            Mockito.argThat(
                Matchers.<SendMessageRequest>hasProperty(
                    "messageBody",
                    new CustomMatcher<String>("valid JSON") {
                        @Override
                        public boolean matches(final Object obj) {
                            final JsonObject json = Json.createReader(
                                new StringReader(obj.toString())
                            ).readObject();
                            return new Time(
                                json.getJsonObject("work")
                                    .getString("scheduled")
                            ).millis() > 0;
                        }
                    }
                )
            )
        );
    }

}
