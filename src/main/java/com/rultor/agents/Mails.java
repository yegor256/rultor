/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
