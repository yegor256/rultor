/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.input.NullInputStream;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Make directory for the daemon.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class MkdirDaemon extends AbstractAgent {

    /**
     * Ctor.
     */
    public MkdirDaemon() {
        super(
            "/talk/shell[host and port and login and key]",
            "/talk/daemon[script and not(dir) and not(started)]"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Shell.Safe(shell).exec(
            "mktemp -d -t rultor-XXXX",
            new NullInputStream(0L),
            baos, baos
        );
        final String dir = baos.toString(StandardCharsets.UTF_8.name()).trim();
        Logger.info(
            this, "directory %s created for %s",
            dir, xml.xpath("/talk/@name").get(0)
        );
        return new Directives()
            .xpath("/talk/daemon[not(started) and not(dir)]")
            .strict(1)
            .add("dir").set(dir);
    }
}
