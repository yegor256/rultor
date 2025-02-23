/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
            try {
                pub &= this.profile.read()
                    .nodes("/p/entry[@key='readers']/item")
                    .isEmpty();
            } catch (final Profile.ConfigException ex) {
                pub = false;
            }
        } catch (final AssertionError ex) {
            pub = false;
        }
        return new Directives()
            .xpath("/talk")
            .attr("public", Boolean.toString(pub));
    }

}
