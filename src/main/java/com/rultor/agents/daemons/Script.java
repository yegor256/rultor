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
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.input.AutoCloseInputStream;

/**
 * Script to run.
 *
 * @since 1.53
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
final class Script {

    /**
     * Script name.
     */
    private final transient String name;

    /**
     * Ctor.
     * @param script Script name
     */
    Script(final String script) {
        this.name = script;
    }

    /**
     * Execute.
     * @param xml Talk xml
     * @return Exit code
     * @throws IOException If fails
     */
    public int exec(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        new Shell.Safe(shell).exec(
            String.format(
                "cd %s && cat > %s && chmod a+x %1$s/%2$s",
                Ssh.escape(dir), Ssh.escape(this.name)
            ),
            AutoCloseInputStream.builder()
                .setInputStream(
                    Objects.requireNonNull(
                        this.getClass().getResourceAsStream(this.name)
                    )
                ).get(),
            Logger.stream(Level.INFO, this),
            Logger.stream(Level.WARNING, this)
        );
        return new Shell.Empty(shell).exec(
            String.join(
                " && ",
                "set -o pipefail",
                String.format("cd %s", Ssh.escape(dir)),
                String.format(
                    "/bin/bash %s >> stdout 2>&1",
                    Ssh.escape(this.name)
                )
            )
        );
    }

}
