/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Sanitizes the daemon, if it's broken.
 *
 * @since 1.54
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class SanitizesDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public SanitizesDaemon() {
        super("/talk/daemon[dir and not(ended)]");
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        final Shell shell = new TalkShells(xml).get();
        final int exit = new Shell.Empty(shell).exec(
            String.format("ls %s", Ssh.escape(dir))
        );
        final Directives dirs = new Directives();
        if (exit != 0) {
            dirs.xpath("/talk/daemon/dir").remove();
            Logger.warn(
                this, "The daemon of %s has lost its directory: %s",
                xml.xpath("/talk/@name").get(0),
                dir
            );
        }
        return dirs;
    }

}
