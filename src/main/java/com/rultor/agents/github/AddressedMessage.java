/**
 * Copyright (c) 2009-2016, rultor.com
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

package com.rultor.agents.github;

import com.jcabi.aspects.Tv;
import com.jcabi.github.Comment;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Xembler;

/**
 * Message that Rultor gives as a reply to a comment.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 *
 */
public class AddressedMessage implements Message {
    /**
     * Comment that needs this.
     */
    private final transient Comment.Smart com;

    /**
     * The text (body) of this message.
     */
    private final transient String text;

    /**
     * Address this message to someone.
     */
    private final transient List<String> logins;

    /**
     * Constructor.
     * @param comt Original comment
     * @param txt Text of the message
     * @param users To whom is this message addressed
     */
    public AddressedMessage(
        final Comment.Smart comt,
        final String txt,
        final List<String> users
    ) {
        this.com = comt;
        this.text = txt;
        if (users.isEmpty()) {
            throw new IllegalStateException(
                "This message needs to be addressed to at least 1 person!"
            );
        }
        this.logins = users;
    }

    /**
     * The comment to which this message replies.
     * @return Github comment.
     */
    public final Comment.Smart comment() {
        return this.com;
    }

    /**
     * The body of this message.
     * @return Text formatted with the original comment.
     * @throws IOException if the original comment couldn't be fetched
     */
    public final String body() throws IOException {
        final StringBuilder msg = new StringBuilder(Tv.HUNDRED);
        final StringBuilder addr = new StringBuilder();
        final String space = " ";
        final String atsym = "@";
        for (final String login : this.logins) {
            addr.append(atsym).append(login).append(space);
        }
        msg.append(
            String.format(
                "> %s\n\n",
                StringUtils.abbreviate(
                    this.com.body().replaceAll("\\p{Space}", space),
                    Tv.HUNDRED
                )
            )
        ).append(addr.toString()).append(this.text);
        return Xembler.escape(msg.toString());
    }
}
