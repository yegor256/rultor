/**
 * Copyright (c) 2009-2015, rultor.com
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

import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Stars repos used.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
public final class Stars extends AbstractAgent {
    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Ctor.
     * @param ghub Github client
     */
    public Stars(final Github ghub) {
        super("/talk/wire[github-repo]");
        this.github = ghub;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Repo repo = this.github.repos().get(
            new Coordinates.Simple(
                xml.nodes("/talk/wire").get(0)
                    .xpath("github-repo/text()").get(0)
            )
        );
        final Coordinates coords = repo.coordinates();
        if (!repo.stars().starred()) {
            repo.stars().star();
        }
        return new Directives();
    }
}
