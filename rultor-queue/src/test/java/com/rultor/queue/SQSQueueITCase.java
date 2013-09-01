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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Queue;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;

/**
 * Integration case for {@link SQSQueue}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class SQSQueueITCase {

    /**
     * AWS key.
     */
    private final transient String key =
        System.getProperty("failsafe.sqs.key");

    /**
     * AWS secret key.
     */
    private final transient String secret =
        System.getProperty("failsafe.sqs.secret");

    /**
     * SQS queue prefix.
     */
    private final transient String prefix =
        System.getProperty("failsafe.sqs.prefix");

    /**
     * SQSQueue can accept and return works.
     * @throws Exception If some problem inside
     */
    @Test
    public void acceptsAndReturnsWorks() throws Exception {
        Assume.assumeNotNull(this.key);
        final AmazonSQS aws = new AmazonSQSClient(
            new BasicAWSCredentials(this.key, this.secret)
        );
        final CreateQueueResult result = aws.createQueue(
            new CreateQueueRequest().withQueueName(
                String.format(
                    "%s%s",
                    this.prefix,
                    RandomStringUtils.randomAlphabetic(Tv.TEN)
                )
            )
        );
        final String url = result.getQueueUrl();
        try {
            final String rule = "some-test-rule";
            final URN owner = new URN("urn:facebook:1");
            final Time time = new Time("2013-07-19T14:05:00Z");
            final Work work = new Work.Simple(owner, rule, time);
            final Queue queue = new SQSQueue(
                new SQSClient.Simple(this.key, this.secret, url)
            );
            queue.push(work);
            MatcherAssert.assertThat(
                queue.pull(1, TimeUnit.SECONDS),
                Matchers.equalTo(work)
            );
        } finally {
            aws.deleteQueue(new DeleteQueueRequest().withQueueUrl(url));
        }
    }

}
