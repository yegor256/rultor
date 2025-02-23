/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Wipe the daemon as broken.
 *
 * @since 1.53
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class WipesDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public WipesDaemon() {
        super("/talk/daemon[started and code and ended and not(dir)]");
    }

    @Override
    public Iterable<Directive> process(final XML xml) {
        Logger.warn(this, "daemon wiped: %s", xml.xpath("/talk/@name").get(0));
        return new Directives()
            .xpath("/talk/daemon")
            .remove();
    }

}
