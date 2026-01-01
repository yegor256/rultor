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
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Kills daemon if too old.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class KillsDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public KillsDaemon() {
        this(TimeUnit.HOURS.toMinutes(1L));
    }

    /**
     * Ctor.
     * @param mins Maximum minutes per build
     */
    public KillsDaemon(final long mins) {
        super(
            "/talk/daemon[started and not(code) and not(ended)]",
            "/talk/daemon/dir",
            String.format(
                // @checkstyle LineLength (1 line)
                "/talk[(current-dateTime() - xs:dateTime(daemon/started)) div xs:dayTimeDuration('PT1M') > %d]",
                mins
        )
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String name = xml.xpath("/talk/@name").get(0);
        try {
            Logger.info(
                this, "The daemon of %s has been killed due to delay, code=%d",
                name, new Script("kill.sh").exec(xml)
            );
        } catch (final IllegalArgumentException ex) {
            Logger.warn(
                this, "We failed to kill the daemon of %s due to delay: %s",
                name, ex.getMessage()
            );
        }
        return new Directives().xpath("/talk/request").remove();
    }

}
