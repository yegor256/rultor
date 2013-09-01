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
package com.rultor.users.pgsql;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.rultor.aws.SQSClient;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Integration case for {@link SQSReceipts}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SQSReceiptsITCase {

    /**
     * JDBC URL.
     */
    private static final String URL =
        System.getProperty("failsafe.pgsql.jdbc");

    /**
     * JDBC password.
     */
    private static final String PASSWORD =
        System.getProperty("failsafe.pgsql.password");

    /**
     * SQSReceipts can process JSON and post to PostrgreSQL.
     * @throws Exception If some problem inside
     */
    @Test
    public void parsesJsonAndPostsToPostrgreSql() throws Exception {
        final SQSClient client = Mockito.mock(SQSClient.class);
        final AmazonSQS aws = Mockito.mock(AmazonSQS.class);
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(
            new ReceiveMessageResult().withMessages(
                new Message().withBody(
                    // @checkstyle StringLiteralsConcatenation (5 lines)
                    "{\"work\": {\"owner\": \"urn:test:1\", \"rule\": \"test\","
                    + " \"scheduled\": \"2012-08-23T13:25:33Z\"},"
                    + "\"ct\":\"urn:test:2\", \"ctrule\":\"some-other-2\","
                    + "\"dt\":\"urn:test:3\", \"dtrule\":\"some-other-4\","
                    + "\"details\":\"тест\", \"amount\": 16458383}"
                )
            )
        ).when(aws).receiveMessage(Mockito.any(ReceiveMessageRequest.class));
        final SQSReceipts receipts = new SQSReceipts(this.pgsql(), client);
        MatcherAssert.assertThat(receipts.process(), Matchers.equalTo(1));
    }

    /**
     * Get PgSql client.
     * @return Sheet to test
     * @throws Exception If some problem inside
     */
    private PgClient pgsql() throws Exception {
        Assume.assumeNotNull(SQSReceiptsITCase.URL);
        return new PgClient.Simple(
            SQSReceiptsITCase.URL, SQSReceiptsITCase.PASSWORD
        );
    }

}
