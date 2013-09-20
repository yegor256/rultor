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
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import com.rultor.aws.SESClient;
import java.util.Arrays;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link SES}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class SESTest {

    /**
     * Constant message.
     */
    public static final String MESSAGE = "message";
    /**
     * Constant subject.
     */
    public static final String SUBJECT = "subject";
    /**
     * Constant data.
     */
    public static final String DATA = "data";
    /**
     * Constant body.
     */
    public static final String BODY = "body";
    /**
     * Constant text.
     */
    public static final String TEXT = "text";
    /**
     * SES can send emails.
     * @throws Exception If some problem inside
     */
    @Test
    public void sendsEmail() throws Exception {
        final SESClient client = Mockito.mock(SESClient.class);
        final AmazonSimpleEmailService aws =
            Mockito.mock(AmazonSimpleEmailService.class);
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(new SendEmailResult()).when(aws)
            .sendEmail(Mockito.any(SendEmailRequest.class));
        final Billboard board = new SES(
            "sender@rultor.com",
            Arrays.asList("recepient@rultor.com"),
            client
        );
        board.announce("hello, друг!\nfirst\nsecond");
        Mockito.verify(aws).sendEmail(
            Mockito.argThat(
                Matchers.<SendEmailRequest>hasProperty(
                    SESTest.MESSAGE,
                    Matchers.allOf(
                        Matchers.<Message>hasProperty(
                            SESTest.SUBJECT,
                            Matchers.<Content>hasProperty(
                                // @checkstyle MultipleStringLiterals (1 line)
                                SESTest.DATA,
                                Matchers.equalTo("hello, друг!")
                            )
                        ),
                        Matchers.<Message>hasProperty(
                            SESTest.BODY,
                            Matchers.<Body>hasProperty(
                                SESTest.TEXT,
                                Matchers.<Content>hasProperty(
                                    SESTest.DATA,
                                    Matchers.equalTo("first\nsecond")
                                )
                            )
                        )
                    )
                )
            )
        );
    }
    /**
     * SES can send emails.
     * @throws Exception If some problem inside
     */
    @Test
    public void sendsEmailFromHTML() throws Exception {
        final SESClient client = Mockito.mock(SESClient.class);
        final String msg = "<html><head><title>t</title></head></html>";
        final AmazonSimpleEmailService aws =
            Mockito.mock(AmazonSimpleEmailService.class);
        Mockito.doReturn(aws).when(client).get();
        Mockito.doReturn(new SendEmailResult()).when(aws)
            .sendEmail(Mockito.any(SendEmailRequest.class));
        final Billboard board = new SES(
            "senderhtml@rultor.com",
            Arrays.asList("recepienthtml@rultor.com"),
            client
        );
        board.announce(msg);
        Mockito.verify(aws).sendEmail(
            Mockito.argThat(
                Matchers.<SendEmailRequest>hasProperty(
                    SESTest.MESSAGE,
                    Matchers.allOf(
                        Matchers.<Message>hasProperty(
                            SESTest.SUBJECT,
                            Matchers.<Content>hasProperty(
                                // @checkstyle MultipleStringLiterals (1 line)
                                SESTest.DATA,
                                Matchers.equalTo("t")
                            )
                        ),
                        Matchers.<Message>hasProperty(
                            SESTest.BODY,
                            Matchers.<Body>hasProperty(
                                "html",
                                Matchers.<Content>hasProperty(
                                    SESTest.DATA,
                                    Matchers.equalTo(msg)
                                )
                            )
                        )
                    )
                )
            )
        );
    }

}
