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

import java.io.IOException;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;
import org.apache.commons.lang3.StringUtils;

/**
 * Message that Rultor gives as a reply to a comment.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 *
 */
abstract class Message {
    /**
     * Comment that needs this 
     */
    final Comment.Smart com;
    final String text;
    Message(Comment.Smart com, String text) {
        this.com = com;
        this.text = text;
    }

    /**
     * The issue where this message is should be posted.
     * @return Github issue.
     */
    Issue issue() {
	    return this.com.issue();
    }

    /**
     * The body of this message.
     * @return
     */
    String body() throws IOException {
        final StringBuilder msg = new StringBuilder(Tv.HUNDRED);
        msg.append(
            String.format(
                "> %s\n\n",
                StringUtils.abbreviate(
                    this.com.body().replaceAll("\\p{Space}", " "),
                    Tv.HUNDRED
                )
            )
        ).append("%s ").append(text);
        return msg.toString();
    }
}
