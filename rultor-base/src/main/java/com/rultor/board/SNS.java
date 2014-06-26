/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.board;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.aws.SNSClient;
import com.rultor.snapshot.Step;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon SNS.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "client", "topic" })
@Loggable(Loggable.DEBUG)
public final class SNS implements Billboard {

    /**
     * SNS client.
     */
    private final transient SNSClient client;

    /**
     * SNS topic.
     */
    private final transient String topic;

    /**
     * Bill to announce.
     */
    private final transient Bill bill;

    /**
     * Public ctor.
     * @param arn ARN of SNS topic
     * @param key AWS key
     * @param secret AWS secret
     * @param bll Bill to announce
     * @checkstyle ParameterNumber (5 lines)
     */
    public SNS(final String arn, final String key, final String secret,
        final Bill bll) {
        this(arn, new SNSClient.Simple(key, secret), bll);
    }

    /**
     * Public ctor.
     * @param arn ARN of SNS topic
     * @param clnt SNS Client
     * @param bll Bill to announce
     */
    public SNS(final String arn, final SNSClient clnt, final Bill bll) {
        this.client = clnt;
        this.topic = arn;
        this.bill = bll;
    }

    @Override
    @Step("notification sent to ${this.topic}")
    public void announce(final boolean success) throws IOException {
        final AmazonSNS aws = this.client.get();
        try {
            aws.publish(
                new PublishRequest()
                    .withTopicArn(this.topic)
                    .withMessage(this.bill.body())
                    .withSubject(this.bill.subject())
            );
        } finally {
            aws.shutdown();
        }
    }

}
