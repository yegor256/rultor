/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.Time;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * If the daemon is too old and the Docker container is already gone.
 *
 * @since 1.72
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class DropsDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public DropsDaemon() {
        // @checkstyle MagicNumber (1 line)
        this(TimeUnit.DAYS.toMinutes(10L));
    }

    /**
     * Ctor.
     * @param mins Maximum minutes per build
     */
    public DropsDaemon(final long mins) {
        super(
            "/talk/daemon[started and not(code) and not(ended)]",
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
        final Shell shell = new TalkShells(xml).get();
        final String talk = xml.xpath("/talk/@name").get(0);
        final String container = new Container(talk).toString();
        final int exit = new Shell.Empty(shell).exec(
            String.format(
                "docker ps | grep %s",
                Ssh.escape(container)
            )
        );
        final Directives dirs = new Directives();
        if (exit != 0) {
            Logger.warn(
                this,
                "Docker container %s is lost in %s, time to drop the daemon",
                container, talk
            );
            dirs.append(
                new Directives()
                    .xpath("/talk/daemon")
                    .strict(1)
                    .add("ended").set(new Time().iso()).up()
                    .add("code").set("1").up()
                    .add("tail").set(
                        Xembler.escape(
                            String.format(
                                "Docker container %s is lost",
                                container
                            )
                        )
                    )
            );
        }
        return dirs;
    }

}
