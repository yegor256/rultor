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
package com.rultor.board;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.aws.SESClient;
import com.rultor.snapshot.Step;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Amazon SES.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "client", "bill" })
@Loggable(Loggable.DEBUG)
public final class SES implements Billboard {

    /**
     * SES client.
     */
    private final transient SESClient client;

    /**
     * Bill to publish.
     */
    private final transient Bill bill;

    /**
     * Public ctor.
     * @param bll Bill
     * @param key AWS key
     * @param secret AWS secret
     */
    public SES(final Bill bll, final String key, final String secret) {
        this(bll, new SESClient.Simple(key, secret));
    }

    /**
     * Public ctor.
     * @param bll Bill
     * @param clnt SES Client
     */
    public SES(final Bill bll, final SESClient clnt) {
        this.client = clnt;
        this.bill = bll;
    }

    @Override
    @Step("email sent to ${this.bill.recipients()}")
    public void announce(final boolean success) throws IOException {
        final AmazonSimpleEmailService aws = this.client.get();
        try {
            final Message message = new Message()
                .withSubject(new Content().withData(this.bill.subject()))
                .withBody(
                    new Body().withText(
                        new Content().withData(this.bill.body())
                    )
                );
            final SendEmailResult result = aws.sendEmail(
                new SendEmailRequest()
                    .withSource(this.bill.sender())
                    .withReturnPath(this.bill.sender())
                    .withDestination(
                        new Destination().withToAddresses(
                            this.bill.recipients()
                        )
                    )
                    .withMessage(message)
            );
            Logger.info(
                this,
                "#announce(..): sent SES email %s from %s to %s",
                result.getMessageId(),
                this.bill.sender(),
                this.bill.recipients()
            );
        } finally {
            aws.shutdown();
        }
    }

}
