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
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.Text;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.Mapped;
import org.cactoos.iterable.Skipped;
import org.cactoos.list.ListOf;
import org.cactoos.text.Joined;
import org.cactoos.text.Split;
import org.cactoos.text.StartsWith;
import org.cactoos.text.Sub;
import org.cactoos.text.TextOf;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Marks the daemon as done.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false)
public final class EndsDaemon extends AbstractAgent {

    /**
     * Prefix for log highlights.
     */
    public static final String HIGHLIGHTS_PREFIX = "RULTOR: ";

    /**
     * Ctor.
     */
    public EndsDaemon() {
        super(
            "/talk/daemon[started and not(code) and not(ended)]",
            "/talk/daemon/dir"
        );
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        final int exit = new Script("end.sh").exec(xml);
        final Directives dirs = new Directives();
        if (exit == 0) {
            Logger.info(
                this, "The daemon is still running in %s (%s)",
                dir, xml.xpath("/talk/@name").get(0)
            );
        } else {
            dirs.append(this.end(shell, dir));
        }
        return dirs;
    }

    /**
     * End this daemon.
     * @param shell Shell
     * @param dir The dir
     * @return Directives
     * @throws IOException If fails
     * @todo #1207:1h There is no limit of tail message (only shifting to
     *  100_000 symbols), but TkDaemon has a limit of 100_000 symbols in buffer
     *  It is better to have a restriction for the tail length, not about start
     *  position.
     */
    private Iterable<Directive> end(final Shell shell,
        final String dir) throws IOException {
        final int exit = EndsDaemon.exit(shell, dir);
        final List<Text> lines = new ListOf<>(
            new Split(
                new TextOf(
                    EndsDaemon.stdout(shell, dir)
                ),
                System.lineSeparator()
            )
        );
        final String highlights = new Joined(
            "\n",
            new Mapped<>(
                s -> new Sub(
                    s,
                    EndsDaemon.HIGHLIGHTS_PREFIX.length()
                ).asString(),
                new Filtered<>(
                    input -> new StartsWith(
                        input,
                        new TextOf(EndsDaemon.HIGHLIGHTS_PREFIX)
                    ).value(),
                    lines
                )
            )
        ).toString();
        Logger.info(this, "daemon finished at %s, exit: %d", dir, exit);
        return new Directives()
            .xpath("/talk/daemon")
            .strict(1)
            .add("ended").set(new Time().iso()).up()
            .add("code").set(Integer.toString(exit)).up()
            .add("highlights").set(Xembler.escape(highlights)).up()
            .add("tail")
            .set(
                Xembler.escape(
                    new Sub(
                        new Joined(
                            System.lineSeparator(),
                            new Skipped<>(
                                Math.max(lines.size() - 60, 0),
                                new ListOf<>(
                                    new Mapped<>(
                                        Text::asString,
                                        lines
                                    )
                                )
                            )
                        ),
                        0,
                        10_000
                    ).toString()
                )
            );
    }

    /**
     * Get exit code.
     * @param shell Shell
     * @param dir The dir
     * @return Exit code
     * @throws IOException If fails
     */
    private static int exit(final Shell shell, final String dir)
        throws IOException {
        final String status = new ShellCommand(
            shell,
            dir,
            "if [ ! -e status ]; then echo 127; exit; fi; cat status"
        ).exec().trim().replaceAll("[^0-9]", "");
        final int exit;
        if (status.isEmpty()) {
            exit = 1;
        } else {
            exit = Integer.parseInt(status);
        }
        return exit;
    }

    /**
     * Get stdout.
     * @param shell Shell
     * @param dir The dir
     * @return Stdout
     * @throws IOException If fails
     */
    private static CharSequence stdout(final Shell shell, final String dir)
        throws IOException {
        final int max = 4_000_000;
        return new ShellCommand(
            shell,
            dir,
            String.join(
                ";",
                "size=$(stat -c%s stdout)",
                String.format("if [ $size -gt %d ]", max),
                "then echo \"Output is too big ($size bytes)\"",
                String.format("echo \"You see only the last %d bytes\"", max),
                String.format("tail -c %d stdout", max),
                "else cat stdout",
                "fi"
            )
        ).exec();
    }

}
