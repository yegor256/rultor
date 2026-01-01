/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Drops talk if there is no 'wire' in it, but it's still
 * set to 'later' processing.
 *
 * @since 1.72
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class DropsTalk extends AbstractAgent {

    /**
     * Ctor.
     */
    public DropsTalk() {
        super("/talk[not(wire) and @later='true']");
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        Logger.info(
            this, "The talk %s is lost, has no wire, dropped it",
            xml.xpath("/talk/@name").get(0)
        );
        return new Directives()
            .xpath("/talk")
            .attr("later", Boolean.toString(false));
    }

}
