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
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Queue;
import com.rultor.spi.Work;
import com.rultor.tools.Exceptions;
import com.rultor.tools.NormJson;
import com.rultor.tools.Time;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.stream.JsonGenerator;
import javax.validation.constraints.NotNull;
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
@SuppressWarnings("PMD.ExcessiveImports")
public final class SQSQueue implements Queue {

    /**
     * JSON schema-based reader.
     */
    private static final NormJson NORM = new NormJson(
        SQSQueue.class.getResourceAsStream("work.json")
    );

    /**
     * JSON key.
     */
    private static final String KEY_OWNER = "urn";

    /**
     * JSON key.
     */
    private static final String KEY_RULE = "rule";

    /**
     * JSON key.
     */
    private static final String KEY_SCHEDULED = "scheduled";

    /**
     * SQS client.
     */
    private final transient SQSClient client;

    /**
     * Public ctor.
     * @param clnt S3 client
     */
    public SQSQueue(@NotNull(message = "SQS client can't be NULL")
        final SQSClient clnt) {
        final AmazonSQS aws = clnt.get();
        final GetQueueAttributesResult result = aws.getQueueAttributes(
            new GetQueueAttributesRequest()
                .withQueueUrl(clnt.url())
        );
        Logger.info(
            SQSQueue.class, "SQS queue is ready with %s",
            result.getAttributes()
        );
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(@NotNull(message = "work can't be NULL") final Work work) {
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
    @NotNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public Work pull(final int limit,
        @NotNull(message = "unit can't be NULL") final TimeUnit unit)
        throws InterruptedException {
        final long start = System.currentTimeMillis();
        Work work;
        while (true) {
            final long delay = System.currentTimeMillis() - start;
            if (delay > unit.toMillis(limit)) {
                work = new Work.None();
                break;
            }
            work = this.fetch((int) unit.toSeconds(limit));
            if (!work.equals(new Work.None())) {
                break;
            }
            TimeUnit.SECONDS.sleep(Tv.FIVE);
        }
        return work;
    }

    /**
     * Fetch next available work or NONE if nothing found.
     * @param sec Seconds to wait
     * @return Work or Work.None
     */
    private Work fetch(final int sec) {
        final AmazonSQS aws = this.client.get();
        Work work = new Work.None();
        try {
            final ReceiveMessageResult result = aws.receiveMessage(
                new ReceiveMessageRequest()
                    .withQueueUrl(this.client.url())
                    .withMaxNumberOfMessages(1)
                    .withWaitTimeSeconds(sec)
            );
            if (!result.getMessages().isEmpty()) {
                final Message msg = result.getMessages().get(0);
                try {
                    work = SQSQueue.unserialize(msg.getBody());
                } catch (NormJson.JsonException ex) {
                    Exceptions.warn(this, ex);
                } finally {
                    aws.deleteMessage(
                        new DeleteMessageRequest()
                            .withQueueUrl(this.client.url())
                            .withReceiptHandle(msg.getReceiptHandle())
                    );
                }
            }
        } finally {
            aws.shutdown();
        }
        return work;
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
            .write(SQSQueue.KEY_SCHEDULED, work.scheduled().toString())
            .write(SQSQueue.KEY_RULE, work.rule())
            .writeEnd()
            .close();
        return writer.toString();
    }

    /**
     * Un-serialize text to work.
     * @param text Text to un-serialize
     * @return Work
     * @throws NormJson.JsonException If can't parse it
     * @checkstyle RedundantThrows (5 lines)
     */
    private static Work unserialize(final String text)
        throws NormJson.JsonException {
        final JsonObject object = SQSQueue.NORM.readObject(text);
        return new Work.Simple(
            URN.create(object.getString(SQSQueue.KEY_OWNER)),
            object.getString(SQSQueue.KEY_RULE),
            new Time(object.getString(SQSQueue.KEY_SCHEDULED))
        );
    }

}
