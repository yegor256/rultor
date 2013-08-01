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
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Stand;
import com.rultor.spi.Users;
import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonObject;
import lombok.EqualsAndHashCode;

/**
 * Sensor to pulses in SQS queue.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.SECONDS)
@EqualsAndHashCode(of = { "users", "client" })
@SuppressWarnings("PMD.DoNotUseThreads")
public final class SQSPulseSensor implements Runnable, Closeable {

    /**
     * Users.
     */
    private final transient Users users;

    /**
     * Quartz queue client.
     */
    private final transient SQSClient client;

    /**
     * Public ctor.
     * @param usr Users
     * @param clnt SQS client for quartz queue
     */
    protected SQSPulseSensor(final Users usr, final SQSClient clnt) {
        this.users = usr;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void run() {
        final AmazonSQS aws = this.client.get();
        final ReceiveMessageResult result = aws.receiveMessage(
            new ReceiveMessageRequest()
                .withQueueUrl(this.client.url())
                .withWaitTimeSeconds(0)
                .withVisibilityTimeout(Tv.FIVE)
                .withMaxNumberOfMessages(Tv.TEN)
        );
        for (Message msg : result.getMessages()) {
            this.post(msg.getBody());
            aws.deleteMessage(
                new DeleteMessageRequest()
                    .withQueueUrl(this.client.url())
                    .withReceiptHandle(msg.getReceiptHandle())
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // nothing to do
    }

    /**
     * Post this JSON to the right stand.
     * @param json JSON to process
     */
    private void post(final String json) {
        final JsonObject object = Json.createReader(
            new StringReader(json)
        ).readObject();
        final Stand stand = this.users
            .get(URN.create(object.getString("user")))
            .stands()
            .get(object.getString("stand"));
        stand.post(
                String.format(
                    "%s:%s:%s",
                    object.getString("work.owner"),
                    object.getString("work.unit"),
                    object.getString("work.started")
                ),
                object.getString("xembly")
            );
    }

}
