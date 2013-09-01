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
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.rultor.aws.SQSClient;
import com.rultor.spi.ACL;
import com.rultor.spi.Arguments;
import com.rultor.spi.Repo;
import com.rultor.spi.SpecException;
import com.rultor.spi.Stand;
import com.rultor.spi.User;
import com.rultor.spi.Users;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.tools.Exceptions;
import com.rultor.tools.NormJson;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.json.JsonObject;
import lombok.EqualsAndHashCode;

/**
 * Sensor to pulses in SQS queue.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = { "users", "client" })
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.ExcessiveImports" })
public final class SQSPulseSensor implements Runnable, Closeable {

    /**
     * JSON schema-based reader.
     */
    private static final NormJson NORM = new NormJson(
        SQSPulseSensor.class.getResourceAsStream("pulse.json")
    );

    /**
     * How many threads to use.
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * Tv.FIVE;

    /**
     * Executor service.
     */
    private final transient ScheduledExecutorService service =
        Executors.newScheduledThreadPool(
            SQSPulseSensor.THREADS, new VerboseThreads()
        );

    /**
     * Users.
     */
    private final transient Users users;

    /**
     * Repository for ACL instantiation.
     */
    private final transient Repo repo;

    /**
     * Quartz queue client.
     */
    private final transient SQSClient client;

    /**
     * Public ctor.
     * @param usr Users
     * @param rpo Repo
     * @param clnt SQS client for quartz queue
     */
    protected SQSPulseSensor(final Users usr, final Repo rpo,
        final SQSClient clnt) {
        this.users = usr;
        this.repo = rpo;
        this.client = clnt;
        final Runnable runnable = new VerboseRunnable(this, true, false);
        for (int thread = 0; thread < SQSPulseSensor.THREADS; ++thread) {
            this.service.scheduleWithFixedDelay(
                runnable, TimeUnit.SECONDS.toMillis(1), 1,
                TimeUnit.MILLISECONDS
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings({
        "PMD.AvoidInstantiatingObjectsInLoops",
        "PMD.AvoidCatchingGenericException"
    })
    public void run() {
        final AmazonSQS aws = this.client.get();
        final ReceiveMessageResult result = aws.receiveMessage(
            new ReceiveMessageRequest()
                .withQueueUrl(this.client.url())
                .withWaitTimeSeconds(Tv.TWENTY)
                .withVisibilityTimeout(Tv.FIVE)
                .withMaxNumberOfMessages(Tv.TEN)
        );
        for (Message msg : result.getMessages()) {
            try {
                this.post(SQSPulseSensor.NORM.readObject(msg.getBody()));
            } catch (SecurityException ex) {
                Exceptions.info(this, ex);
            // @checkstyle IllegalCatch (1 line)
            } catch (Stand.BrokenXemblyException ex) {
                Exceptions.warn(this, ex);
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
        aws.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.service.shutdown();
    }

    /**
     * Post this JSON to the right stand.
     * @param json JSON to process
     * @throws Stand.BrokenXemblyException If fails
     * @checkstyle RedundantThrows (4 lines)
     */
    private void post(final JsonObject json)
        throws Stand.BrokenXemblyException {
        final Stand stand = this.users.stand(json.getString("stand"));
        final String key = json.getString("key");
        if (!this.acl(stand).canPost(key)) {
            throw new SecurityException(
                String.format(
                    "access denied to `%s` for '%s'",
                    stand.name(), key
                )
            );
        }
        final JsonObject work = json.getJsonObject("work");
        stand.post(
            String.format(
                "%s %s %s",
                work.getString("scheduled"),
                work.getString("owner"),
                work.getString("rule")
            ),
            json.getJsonNumber("nano").longValue(),
            json.getString("xembly")
        );
    }

    /**
     * Get ACL of a stand.
     * @param stand The stand
     * @return ACL
     */
    private ACL acl(final Stand stand) {
        try {
            return ACL.class.cast(
                new Repo.Cached(this.repo, new User.Nobody(), stand.acl())
                    .get()
                    .instantiate(
                        this.users,
                        new Arguments(new Work.None(), new Wallet.Empty())
                    )
            );
        } catch (SpecException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
