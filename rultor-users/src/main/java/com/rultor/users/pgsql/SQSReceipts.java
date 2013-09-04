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
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.VoidHandler;
import com.rultor.aws.SQSClient;
import com.rultor.tools.Exceptions;
import com.rultor.tools.NormJson;
import com.rultor.tools.Time;
import java.sql.SQLException;
import javax.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Receipts coming from SQS.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "client")
@Loggable(Loggable.DEBUG)
final class SQSReceipts {

    /**
     * JSON schema-based reader.
     */
    private static final NormJson NORM = new NormJson(
        SQSReceipts.class.getResourceAsStream("receipt.json")
    );

    /**
     * Mongo container.
     */
    private final transient PgClient client;

    /**
     * SQS queue.
     */
    private final transient SQSClient queue;

    /**
     * Public ctor.
     * @param clnt Client
     * @param sqs SQS queue
     */
    protected SQSReceipts(final PgClient clnt, final SQSClient sqs) {
        this.client = clnt;
        this.queue = sqs;
    }

    /**
     * Fetch and process next portions of them.
     * @return How many messages were processed
     * @throws SQLException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public int process() throws SQLException {
        final AmazonSQS aws = this.queue.get();
        final ReceiveMessageResult result = aws.receiveMessage(
            new ReceiveMessageRequest()
                .withQueueUrl(this.queue.url())
                .withWaitTimeSeconds(Tv.TWENTY)
                .withVisibilityTimeout(Tv.FIVE)
                .withMaxNumberOfMessages(Tv.TEN)
        );
        for (Message msg : result.getMessages()) {
            try {
                this.process(SQSReceipts.NORM.readObject(msg.getBody()));
            } catch (NormJson.JsonException ex) {
                Exceptions.warn(this, ex);
            } finally {
                aws.deleteMessage(
                    new DeleteMessageRequest()
                        .withQueueUrl(this.queue.url())
                        .withReceiptHandle(msg.getReceiptHandle())
                );
            }
        }
        aws.shutdown();
        return result.getMessages().size();
    }

    /**
     * Process one JSON message.
     * @param json Message in JSON
     * @throws SQLException If fails
     */
    private void process(final JsonObject json) throws SQLException {
        final JsonObject work = json.getJsonObject("work");
        new JdbcSession(this.client.get())
            // @checkstyle LineLength (1 line)
            .sql("INSERT INTO receipt (time, wowner, wrule, wscheduled, ct, ctrule, dt, dtrule, details, amount) VALUES (now(), ?, ?, ?, ?, ?, ?, ?, ?, ?)")
            .set(work.getString("owner"))
            .set(work.getString("rule"))
            .set(new Time(work.getString("scheduled")).toString())
            .set(json.getString("ct"))
            .set(json.getString("ctrule"))
            .set(json.getString("dt"))
            .set(json.getString("dtrule"))
            .set(json.getString("details"))
            .set(json.getJsonNumber("amount").longValue())
            .insert(new VoidHandler());
    }

}
