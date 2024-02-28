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
package com.rultor.agents.aws;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.PfShell;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Connects a running EC2 instance: detects its IP.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class ConnectsInstance extends AbstractAgent {

    /**
     * Shell.
     */
    private final transient PfShell shell;

    /**
     * Ctor.
     * @param shll The shell
     */
    public ConnectsInstance(final PfShell shll) {
        super(
            "/talk[daemon and ec2 and not(shell)]",
            "/talk/ec2/host"
        );
        this.shell = shll;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String instance = xml.xpath("/talk/ec2/@id").get(0);
        final String host = xml.xpath("/talk/ec2/host").get(0);
        final Directives dirs = new Directives();
        if (this.alive(host)) {
            Logger.warn(
                this, "AWS instance %s is alive at %s for %s",
                instance, host, xml.xpath("/talk/@name").get(0)
            );
            dirs.xpath("/talk").add("shell")
                .attr("id", xml.xpath("/talk/daemon/@id").get(0))
                .add("host").set(host).up()
                .add("port").set(Integer.toString(this.shell.port())).up()
                .add("login").set(this.shell.login()).up()
                .add("key").set(this.shell.key());
            Logger.info(
                this, "AWS instance %s launched and running at %s",
                instance, host
            );
        } else {
            Logger.warn(
                this, "Can't connect to AWS instance %s at %s",
                instance, host
            );
        }
        return dirs;
    }

    /**
     * Tries to connect to it via SSH and returns TRUE if it's possible.
     * @param host IP of the host
     * @return TRUE if alive
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private boolean alive(final String host) {
        boolean alive = false;
        try {
            new Shell.Empty(
                new Shell.Safe(
                    this.shell.withHost(host).toSsh()
                )
            ).exec("whoami");
            alive = true;
            // @checkstyle IllegalCatchCheck (1 line)
        } catch (final Exception ex) {
            Logger.warn(
                this, "Failed to SSH-connect to %s: %s",
                host, ex.getMessage()
            );
        }
        return alive;
    }
}
