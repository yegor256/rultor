/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.Time;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

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
        } catch (final UnknownHostException ex) {
            Logger.warn(
                this, "The host %s is unreachable: %s",
                DismountDaemon.host(xml),
                ex.getMessage()
            );
            dirs.append(
                new Directives()
                    .xpath("/talk/daemon")
                    .strict(1)
                    .add("ended").set(new Time().iso()).up()
                    .add("code").set("1").up()
                    .add("tail").set(
                        Xembler.escape(
                            String.join(
                                "\n",
                                String.format(
                                    "The host %s is not reachable",
                                    DismountDaemon.host(xml)
                                ),
                                "The daemon is older than a few days",
                                "The daemon is marked as done",
                                ex.getMessage(),
                                "Please, try to run the daemon again"
                            )
                        )
                    )
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
