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
import com.jcabi.matchers.XhtmlMatchers;
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
        final String subject = "hello, друг!";
        final Billboard board = new SES(
            new Bill.Simple(
                subject,
                "sender@rultor.com",
                Arrays.asList("recepient@rultor.com")
            ),
            client
        );
        board.announce(true);
        Mockito.verify(aws).sendEmail(
            Mockito.argThat(
                Matchers.<SendEmailRequest>hasProperty(
                    "message",
                    Matchers.allOf(
                        Matchers.<Message>hasProperty(
                            "subject",
                            Matchers.<Content>hasProperty(
                                // @checkstyle MultipleStringLiterals (1 line)
                                "data",
                                Matchers.equalTo(subject)
                            )
                        ),
                        Matchers.<Message>hasProperty(
                            "body",
                            Matchers.<Body>hasProperty(
                                "text",
                                Matchers.<Content>hasProperty(
                                    "data",
                                    XhtmlMatchers.hasXPath("/snapshot")
                                )
                            )
                        )
                    )
                )
            )
        );
    }

}
