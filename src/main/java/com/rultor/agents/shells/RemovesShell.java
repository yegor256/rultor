/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.shells;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Removes shell.
 *
 * @since 1.3
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class RemovesShell extends AbstractAgent {

    /**
     * Ctor.
     */
    public RemovesShell() {
        super(
            "/talk/shell[@id]",
            "/talk[not(daemon)]"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) {
        return new Directives().xpath("/talk/shell").strict(1).remove();
    }
}
