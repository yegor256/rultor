/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Marks the daemon as done when the host is not reachable and the
 * daemon is older than a few days.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class DismountDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public DismountDaemon() {
        // @checkstyle MagicNumber (1 line)
        this(TimeUnit.DAYS.toMinutes(10L));
    }

    /**
     * Ctor.
     * @param mins Maximum minutes per build
     */
    public DismountDaemon(final long mins) {
        super(
            "/talk/daemon[started and dir]",
            String.format(
                // @checkstyle LineLength (1 line)
                "/talk[(current-dateTime() - xs:dateTime(daemon/started)) div xs:dayTimeDuration('PT1M') > %d]",
                mins
            ),
            "/talk/shell[host and port and login and key]"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Directives dirs = new Directives();
        try {
            Logger.info(
                this, "Checking %s...",
                DismountDaemon.host(xml)
            );
            new Shell.Empty(new TalkShells(xml).get()).exec("pwd");
            Logger.info(
                this, "The host %s is alive",
                DismountDaemon.host(xml)
            );
        } catch (final IOException ex) {
            Logger.warn(
                this, "The host %s is unreachable: %s",
                DismountDaemon.host(xml),
                ex.getMessage()
            );
            dirs.append(
                new Directives()
                    .xpath("/talk/daemon")
                    .strict(1)
                    .remove()
            );
        }
        return dirs;
    }

    /**
     * Host name of the daemon.
     * @param talk The talk
     * @return Host name
     */
    private static String host(final XML talk) {
        return String.format(
            "%s:%s for %s",
            talk.xpath("/talk/shell/host/text()").get(0),
            talk.xpath("/talk/shell/port/text()").get(0),
            talk.xpath("/talk/@name").get(0)
        );
    }

}
