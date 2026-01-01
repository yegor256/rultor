/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

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
 * Stops daemon if STOP request is present.
 *
 * @since 1.50
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class StopsDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public StopsDaemon() {
        super(
            "/talk/daemon/dir",
            "/talk/request[type='stop']",
            "/talk/daemon[started and not(code) and not(ended)]"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        Logger.info(
            this, "docker stop attempt at %s, code=%d",
            xml.xpath("/talk/@name").get(0),
            new Script("stop.sh").exec(xml)
        );
        return new Directives();
    }

}
