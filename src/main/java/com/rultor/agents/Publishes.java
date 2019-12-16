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
package com.rultor.agents;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Publishes if it's public.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.32.7
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "profile")
public final class Publishes extends AbstractAgent {

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Ctor.
     * @param prf Profile
     * @param ghub GitHub
     */
    public Publishes(final Profile prf, final Github ghub) {
        super(
            "/talk[@public!='false']",
            "/talk/archive/log"
        );
        this.profile = prf;
        this.github = ghub;
    }

    @Override
    @SuppressWarnings("PMD.BooleanInversion")
    public Iterable<Directive> process(final XML xml) throws IOException {
        boolean pub;
        try {
            pub = !new Repo.Smart(
                this.github.repos().get(
                    new Coordinates.Simple(this.profile.name())
                )
            ).isPrivate();
        } catch (final AssertionError ex) {
            pub = false;
        }
        try {
            pub &= this.profile.read()
                .nodes("/p/entry[@key='readers']/item")
                .isEmpty();
        } catch (final Profile.ConfigException ex) {
            pub = false;
        }
        return new Directives()
            .xpath("/talk")
            .attr("public", Boolean.toString(pub));
    }

}
