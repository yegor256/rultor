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
            "/talk/ec2",
            "/talk/daemon",
            "/talk/shell[host and port and login and key]"
        );
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        final String instance = xml.xpath("/talk/ec2/@id").get(0);
        final String host = xml.xpath("/talk/shell/host/text()").get(0);
        final Directives dirs = new Directives();
        int attempt = 0;
        while (true) {
            try {
                new Shell.Empty(new Shell.Safe(shell)).exec("whoami");
                Logger.warn(
                    this, "AWS instance %s is alive at %s for %s",
                    instance, host, xml.xpath("/talk/@name").get(0)
                );
                break;
            // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception ex) {
                Logger.warn(
                    this, "Failed to ping AWS instance %s at %s (attempt no.%d): %s",
                    instance, host, attempt, ex.getMessage()
                );
                ++attempt;
                if (attempt > 5) {
                    dirs.xpath("/talk/daemon").remove();
                    break;
                }
                new Sleep(10L).now();
            }
        }
        return dirs;
    }
}
