/**
 * Copyright (c) 2009-2019, Yegor Bugayenko
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

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Closes pull request manually, leaves comment with description.
 *
 * @author Viktor Kuchyn (kuchin.victor@gmail.com)
 * @version $Id$
 * @since 1.63
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = {"github", "profile"})
public final class ClosePullRequest extends AbstractAgent {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Profile.
     */
    private final transient Profile.Defaults profile;

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Constructor.
     * @param prof Profile
     * @param ghub Github
     */
    public ClosePullRequest(final Profile prof, final Github ghub) {
        super("/talk/wire[github-repo and github-issue]");
        this.profile = new Profile.Defaults(prof);
        this.github = ghub;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String rebase = this.profile.text(
            "/p/entry[@key='merge']/entry[@key='rebase']",
            "false"
        );
        if ("true".equals(rebase)) {
            final Issue.Smart issue = new TalkIssues(this.github, xml).get();
            issue.close();
            issue.comments().post(
                ClosePullRequest.PHRASES.getString(
                    "ClosePullRequest.explanation"
                )
            );
        }
        return new Directives();
    }
}
