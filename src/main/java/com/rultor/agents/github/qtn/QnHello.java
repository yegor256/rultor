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
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.log.Logger;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import com.rultor.agents.github.MessageToCommentAuthor;
import java.io.IOException;
import java.net.URI;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Hello.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class QnHello implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        new Answer(
            new MessageToCommentAuthor(
                comment, QnHello.PHRASES.getString("QnHello.intro")
            )
        ).post();
        Logger.info(this, "hello found in #%d", comment.issue().number());
        return Req.DONE;
    }

}
