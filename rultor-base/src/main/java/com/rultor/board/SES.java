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
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang.CharUtils;

/**
 * Amazon SES.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = { "client", "sender", "recipients" })
@Loggable(Loggable.DEBUG)
public final class SES implements Billboard {

    /**
     * SES client.
     */
    private final transient SESClient client;

    /**
     * Sender.
     */
    private final transient String sender;

    /**
     * Recipients.
     */
    private final transient String[] recipients;

    /**
     * Public ctor.
     * @param src Sender of emails
     * @param rcpts Recipients
     * @param key AWS key
     * @param secret AWS secret
     * @checkstyle ParameterNumber (4 lines)
     */
    public SES(final String src, final Collection<String> rcpts,
        final String key, final String secret) {
        this(src, rcpts, new SESClient.Simple(key, secret));
    }

    /**
     * Public ctor.
     * @param src Sender of emails
     * @param rcpts Recipients
     * @param clnt SES Client
     */
    public SES(final String src, final Collection<String> rcpts,
        final SESClient clnt) {
        this.client = clnt;
        this.recipients = rcpts.toArray(new String[rcpts.size()]);
        this.sender = src;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "SES emails from %s to %[list]s accessed with %s",
            this.sender, Arrays.asList(this.recipients), this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void announce(@NotNull final Announcement anmt) throws IOException {
        final AmazonSimpleEmailService aws = this.client.get();
        try {
            String print = String.class.cast(anmt.args().get("print"));
            if (print == null) {
                final StringBuilder txt = new StringBuilder();
                for (Map.Entry<String, Object> entry : anmt.args().entrySet()) {
                    txt.append(entry.getKey())
                        .append(':')
                        .append(CharUtils.LF)
                        .append(entry.getValue())
                        .append(CharUtils.LF)
                        .append(CharUtils.LF);
                }
                print = txt.toString();
            }
            final String subject = String.format(
                "%s: %s", anmt.level(), anmt.args().get("title")
            );
            final Message message = new Message()
                .withSubject(new Content().withData(subject))
                .withBody(new Body().withText(new Content().withData(print)));
            final SendEmailResult result = aws.sendEmail(
                new SendEmailRequest()
                    .withSource(this.sender)
                    .withReturnPath(this.sender)
                    .withDestination(
                        new Destination().withToAddresses(this.recipients)
                    )
                    .withMessage(message)
            );
            Logger.info(
                this,
                "#announce(..): sent SES email %s from %s to %[list]s",
                result.getMessageId(),
                this.sender,
                Arrays.asList(this.recipients)
            );
        } finally {
            aws.shutdown();
        }
    }

}
