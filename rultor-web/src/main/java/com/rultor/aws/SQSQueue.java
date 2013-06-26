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
package com.rultor.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.rultor.spi.Queue;
import com.rultor.spi.Spec;
import com.rultor.spi.Work;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Queue in Amazon SQS.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "client")
@Loggable(Loggable.DEBUG)
public final class SQSQueue implements Queue {

    /**
     * JSON key.
     */
    private static final String KEY_OWNER = "urn";

    /**
     * JSON key.
     */
    private static final String KEY_UNIT = "unit";

    /**
     * JSON key.
     */
    private static final String KEY_SPEC = "spec";

    /**
     * JSON key.
     */
    private static final String KEY_STARTED = "started";

    /**
     * SQS client.
     */
    private final transient SQSClient client;

    /**
     * Public ctor.
     * @param clnt S3 client
     */
    public SQSQueue(final SQSClient clnt) {
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(final Work work) {
        final AmazonSQS aws = this.client.get();
        try {
            final SendMessageResult result = aws.sendMessage(
                new SendMessageRequest()
                    .withQueueUrl(this.client.url())
                    .withMessageBody(SQSQueue.serialize(work))
            );
            Logger.debug(
                this,
                "#push(%s): SQS message %s sent",
                work, result.getMessageId()
            );
        } finally {
            aws.shutdown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public Work pull() throws InterruptedException {
        final AmazonSQS aws = this.client.get();
        try {
            final ReceiveMessageRequest request = new ReceiveMessageRequest()
                .withQueueUrl(this.client.url())
                .withMaxNumberOfMessages(1);
            ReceiveMessageResult result;
            while (true) {
                result = aws.receiveMessage(request);
                if (!result.getMessages().isEmpty()) {
                    final Message msg = result.getMessages().get(0);
                    aws.deleteMessage(
                        new DeleteMessageRequest()
                            .withQueueUrl(this.client.url())
                            .withReceiptHandle(msg.getReceiptHandle())
                    );
                    Logger.debug(
                        this,
                        "#pull(): SQS message %s received",
                        msg.getMessageId()
                    );
                    return SQSQueue.unserialize(msg.getBody());
                }
                TimeUnit.SECONDS.sleep(Tv.FIFTEEN);
            }
        } finally {
            aws.shutdown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(final MetricRegistry registry) {
        registry.register(
            MetricRegistry.name(this.getClass(), "queue-size"),
            new Gauge<Integer>() {
                @Override
                public Integer getValue() {
                    return SQSQueue.this.size();
                }
            }
        );
    }

    /**
     * Approximate size of the queue.
     * @return Size of it
     */
    private int size() {
        final AmazonSQS aws = this.client.get();
        try {
            final String name = "ApproximateNumberOfMessages";
            final GetQueueAttributesResult result = aws.getQueueAttributes(
                new GetQueueAttributesRequest()
                    .withAttributeNames(name)
                    .withQueueUrl(this.client.url())
            );
            return Integer.parseInt(result.getAttributes().get(name));
        } finally {
            aws.shutdown();
        }
    }

    /**
     * Serialize work to string.
     * @param work The work to serialize
     * @return Text
     */
    private static String serialize(final Work work) {
        final StringWriter writer = new StringWriter();
        final JsonGenerator generator = Json.createGenerator(writer);
        generator.writeStartObject()
            .write(SQSQueue.KEY_OWNER, work.owner().toString())
            .write(SQSQueue.KEY_STARTED, work.started())
            .write(SQSQueue.KEY_UNIT, work.unit())
            .write(SQSQueue.KEY_SPEC, work.spec().asText())
            .writeEnd()
            .close();
        return writer.toString();
    }

    /**
     * Un-serialize text to work.
     * @param text Text to un-serialize
     * @return Work
     */
    private static Work unserialize(final String text) {
        final JsonReader reader = Json.createReader(new StringReader(text));
        final JsonObject object = reader.readObject();
        return new Work.Simple(
            URN.create(object.getString(SQSQueue.KEY_OWNER)),
            object.getString(SQSQueue.KEY_UNIT),
            new Spec.Simple(object.getString(SQSQueue.KEY_SPEC)),
            object.getJsonNumber(SQSQueue.KEY_STARTED).longValue()
        );
    }

}
