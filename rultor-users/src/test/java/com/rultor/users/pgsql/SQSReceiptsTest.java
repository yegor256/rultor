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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import javax.sql.DataSource;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link SQSReceipts}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SQSReceiptsTest {

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
        final PgClient pgsql = this.pgsql();
        final SQSReceipts receipts = new SQSReceipts(pgsql, client);
        MatcherAssert.assertThat(receipts.process(), Matchers.equalTo(1));
        Mockito.verify(pgsql).get();
    }

    /**
     * SQSReceipts can gracefully ignore broken JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void gracefullyIgnoresBrokenJson() throws Exception {
        final SQSClient client = Mockito.mock(SQSClient.class);
        final AmazonSQS aws = Mockito.mock(AmazonSQS.class);
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(
            new ReceiveMessageResult().withMessages(
                new Message().withBody("this is not a JSON at all")
            )
        ).when(aws).receiveMessage(Mockito.any(ReceiveMessageRequest.class));
        final PgClient pgsql = this.pgsql();
        final SQSReceipts receipts = new SQSReceipts(pgsql, client);
        MatcherAssert.assertThat(receipts.process(), Matchers.equalTo(1));
        Mockito.verify(pgsql, Mockito.never()).get();
    }

    /**
     * SQSReceipts can gracefully ignore incomplete JSON.
     * @throws Exception If some problem inside
     */
    @Test
    public void gracefullyIgnoresIncompleteJson() throws Exception {
        final SQSClient client = Mockito.mock(SQSClient.class);
        final AmazonSQS aws = Mockito.mock(AmazonSQS.class);
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(
            new ReceiveMessageResult().withMessages(
                new Message().withBody(
                    "{\"work\": {\"rule\":\"x\",\"owner\":\"urn:test:1\"}}"
                )
            )
        ).when(aws).receiveMessage(Mockito.any(ReceiveMessageRequest.class));
        final PgClient pgsql = this.pgsql();
        final SQSReceipts receipts = new SQSReceipts(pgsql, client);
        MatcherAssert.assertThat(receipts.process(), Matchers.equalTo(1));
        Mockito.verify(pgsql, Mockito.never()).get();
    }

    /**
     * Get PgSql client.
     * @return Sheet to test
     * @throws Exception If some problem inside
     */
    @SuppressWarnings("PMD.CloseResource")
    private PgClient pgsql() throws Exception {
        final PreparedStatement stmt = Mockito.mock(PreparedStatement.class);
        final Connection conn = Mockito.mock(Connection.class);
        Mockito.doReturn(stmt).when(conn).prepareStatement(
            Mockito.anyString(), Mockito.eq(Statement.RETURN_GENERATED_KEYS)
        );
        final DataSource source = Mockito.mock(DataSource.class);
        Mockito.doReturn(conn).when(source).getConnection();
        final PgClient pgsql = Mockito.mock(PgClient.class);
        Mockito.doReturn(source).when(pgsql).get();
        return pgsql;
    }

}
