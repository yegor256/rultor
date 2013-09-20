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
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.rultor.aws.SESClient;
import com.rultor.snapshot.Step;
import java.io.StringReader;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import lombok.EqualsAndHashCode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Amazon SES.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
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
    private final transient Array<String> recipients;

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
        this.recipients = new Array<String>(rcpts);
        this.sender = src;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "SES from %s to %s with %s",
            this.sender, this.recipients, this.client
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Step("email sent to ${this.recipients}")
    public void announce(
        @NotNull(message = "body can't be NULL") final String body) {
        final AmazonSimpleEmailService aws = this.client.get();
        String[] parts = new String[2];
        if (body.startsWith("<html>") && body.endsWith("</html>")) {
            try {
                final InputSource source =
                    new InputSource(new StringReader(body));
                final DocumentBuilderFactory dbf =
                    DocumentBuilderFactory.newInstance();
                final DocumentBuilder dbuilder = dbf.newDocumentBuilder();
                final Document document = dbuilder.parse(source);
                final XPathFactory xpathFactory = XPathFactory.newInstance();
                final XPath xpath = xpathFactory.newXPath();
                final String msg = xpath.evaluate("/html/head/title", document);
                parts[0] = msg;
                parts[1] = body;
            } catch (Exception ex) {
                throw new RuntimeException(ex.getMessage());
            }
        } else {
            parts = body.split("\n", 2);
        }
        try {
            final Message message = new Message()
                .withSubject(new Content().withData(parts[0]))
                .withBody(
                    new Body().withText(new Content().withData(parts[1]))
                );
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
                "#announce(..): sent SES email %s from %s to %s",
                result.getMessageId(),
                this.sender,
                this.recipients
            );
        } finally {
            aws.shutdown();
        }
    }

}
