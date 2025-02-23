/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.Time;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Ping EC2 instance and deletes "daemon" if it doesn't reply.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class PingsInstance extends AbstractAgent {

    /**
     * Ctor.
     */
    public PingsInstance() {
        super(
            "/talk/ec2/host",
            "/talk/daemon",
            "/talk/shell[host and port and login and key]"
        );
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String name = xml.xpath("/talk/@name").get(0);
        final Shell shell = new TalkShells(xml).get();
        final String instance = xml.xpath("/talk/ec2/instance/text()").get(0);
        final String host = xml.xpath("/talk/shell/host/text()").get(0);
        final Directives dirs = new Directives();
        int attempt = 0;
        while (true) {
            try {
                new Shell.Empty(new Shell.Safe(shell)).exec("whoami");
                Logger.warn(
                    this, "AWS instance %s is alive at %s for %s",
                    instance, host, name
                );
                break;
            // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception ex) {
                Logger.warn(
                    this,
                    "Failed to ping AWS instance %s at %s for %s (attempt no.%d): %s",
                    instance, host, name, attempt, ex.getMessage()
                );
                ++attempt;
                if (attempt > 3) {
                    dirs.xpath("/talk/daemon").strict(1);
                    if (xml.nodes("/talk/daemon/started").isEmpty()) {
                        dirs.remove();
                        Logger.warn(
                            this, "The AWS instance %s is officially dead at %s (never started)",
                            instance, name
                        );
                    } else {
                        dirs.add("ended").set(new Time().iso()).up().add("code").set(1);
                        Logger.warn(
                            this, "The AWS instance %s is officially dead at %s",
                            instance, name
                        );
                    }
                    break;
                }
                new Sleep(1L).now();
            }
        }
        return dirs;
    }
}
