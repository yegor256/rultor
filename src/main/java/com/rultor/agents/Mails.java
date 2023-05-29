/**
 * Copyright (c) 2009-2023 Yegor Bugayenko
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
package com.rultor.agents;

import com.jcabi.aspects.Immutable;
import com.jcabi.email.Postman;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Send email after release done.
 *
 * @author Yuriy Alevohin (alevohin@mail.ru)
 * @version $Id$
 * @since 2.0
 * @todo #748 Implement Mails agent. Similar to what we do in CommentsTag
 *  we should do here - send an email to all listed addresses. The body of
 *  the email should contain similar text to what we create for the tag
 *  comment. Postman's parameters are stored in Manifest.MF. We need 4
 *  parameters: Rultor-SMTPHost, Rultor-SMTPPort, Rultor-SMTPUsername,
 *  Rultor-SMTPPassword. New instance of Mails must be created at Agents,
 *  after CommentsTag.
 * @todo #748 Describe in file 2014-07-13-reference.md config parameters for
 *  email after release and how it works shortly. Config format described
 *  in issue #748.
 */
@Immutable
@ToString
public final class Mails extends AbstractAgent {

    /**
     * Ctor.
     * @param prfl Profile
     * @param pstmn Mail client
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public Mails(final Profile prfl, final Postman pstmn) {
        super(
            "/talk/request[@id and type='release' and success='true']"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        return new Directives();
    }
}
