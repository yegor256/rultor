/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.Array;
import com.jcabi.xml.XML;
import com.rultor.spi.Agent;
import com.rultor.spi.Talk;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;

/**
 * Abstract agent.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "xpaths")
public abstract class AbstractAgent implements Agent {

    /**
     * Encapsulated XPaths.
     */
    private final transient Array<String> xpaths;

    /**
     * Ctor.
     *
     * <p>The list of XPath expressions must all retrieve something from the
     * XML in order for this agent to be executed. Consider them all to be
     * joined with a logical AND.</p>
     *
     * @param args XPath expressions
     */
    public AbstractAgent(final String... args) {
        this.xpaths = new Array<>(args);
    }

    @Override
    public final void execute(final Talk talk) throws IOException {
        if (new Required(this.xpaths).isIt(talk)) {
            talk.modify(this.process(talk.read()));
        }
    }

    /**
     * Process it.
     * @param xml Its xml
     * @return Directives
     * @throws IOException If fails
     */
    protected abstract Iterable<Directive> process(XML xml)
        throws IOException;

}
