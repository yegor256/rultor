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
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.aspects.Tv;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Queue;
import com.rultor.spi.Rule;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * Quartz in Amazon SQS.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.SECONDS)
@EqualsAndHashCode(of = { "users", "queue", "client" })
@SuppressWarnings("PMD.DoNotUseThreads")
public final class SQSQuartz implements Runnable, Closeable {

    /**
     * Users.
     */
    private final transient Users users;

    /**
     * Queue.
     */
    private final transient Queue queue;

    /**
     * Quartz queue client.
     */
    private final transient SQSClient client;

    /**
     * Public ctor.
     * @param usr Users
     * @param que Queue
     * @param clnt SQS client for quartz queue
     */
    protected SQSQuartz(final Users usr, final Queue que,
        final SQSClient clnt) {
        this.users = usr;
        this.queue = que;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Loggable(value = Loggable.DEBUG, limit = 2, unit = TimeUnit.MINUTES)
    public void run() {
        this.publish(this.passed(this.next()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // nothing to do
    }

    /**
     * Wait until this time is in the past.
     * @param time Time to wait for
     * @return The same time
     */
    private Time passed(final Time time) {
        while (time.millis() > System.currentTimeMillis()) {
            try {
                TimeUnit.SECONDS.sleep(Tv.FIVE);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(ex);
            }
        }
        return time;
    }

    /**
     * Pull next execution time from the quartz queue.
     * @return Time
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Time next() {
        final AmazonSQS aws = this.client.get();
        final ReceiveMessageResult result = aws.receiveMessage(
            new ReceiveMessageRequest()
                .withQueueUrl(this.client.url())
                .withWaitTimeSeconds(Tv.TWENTY)
                .withVisibilityTimeout(Tv.FIVE)
                .withMaxNumberOfMessages(Tv.TEN)
        );
        final Time previous;
        if (result.getMessages().isEmpty()) {
            previous = new Time(new Time().toString());
        } else {
            previous = new Time(result.getMessages().get(0).getBody());
        }
        final Time next = new Time(
            previous.millis() + TimeUnit.MINUTES.toMillis(1)
        ).round();
        aws.sendMessage(
            new SendMessageRequest()
                .withQueueUrl(this.client.url())
                .withDelaySeconds(0)
                .withMessageBody(next.toString())
        );
        for (Message msg : result.getMessages()) {
            aws.deleteMessage(
                new DeleteMessageRequest()
                    .withQueueUrl(this.client.url())
                    .withReceiptHandle(msg.getReceiptHandle())
            );
        }
        return previous;
    }

    /**
     * Publish them all with the specified time.
     * @param time Time to use
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void publish(final Time time) {
        for (User user : this.users) {
            for (Rule rule : user.rules()) {
                this.queue.push(new Work.Simple(user.urn(), rule.name(), time));
            }
        }
    }

}
