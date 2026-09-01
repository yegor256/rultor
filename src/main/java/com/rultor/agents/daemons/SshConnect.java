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
import com.rultor.agents.shells.TalkShells;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import org.apache.commons.io.input.NullInputStream;
import org.cactoos.text.Joined;

/**
 * SSH connect.
 * @since 1.1
 */
@Immutable
final class SshConnect implements Connect {

    /**
     * XML of the talk.
     */
    private final transient XML xml;

    /**
     * Ctor.
     * @param talk Talk
     */
    SshConnect(final XML talk) {
        this.xml = talk;
    }

    @Override
    public InputStream read() throws IOException {
        final Shell shell = new TalkShells(this.xml).get();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        shell.exec(
            new Joined(
                "",
                "dir=",
                Ssh.escape(
                    this.xml.xpath("/talk/daemon/dir/text()").get(0)
                ),
                ";",
                " (cat \"${dir}/stdout\" 2>/dev/null",
                " || echo \"file $file is gone\")",
                " | iconv -f utf-8 -t utf-8 -c",
                " | LANG=en_US.UTF-8 col -b"
            ).toString(),
            new NullInputStream(0L), baos,
            Logger.stream(Level.SEVERE, true)
        );
        return new ByteArrayInputStream(baos.toByteArray());
    }
}
