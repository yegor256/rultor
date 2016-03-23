/**
 * Copyright (c) 2009-2016, rultor.com
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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.Time;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;

/**
 * Marks the daemon as done.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @todo #1053:30min Reduce the data abstraction coupling of EndsDaemon in order
 *  to get rid of the checkstyle suppression of
 *  ClassDataAbstractionCouplingCheck
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
     * The profile of the run.
     */
    private final transient Profile profile;

    /**
     * Ctor.
     * @param prof Profile
     */
    public EndsDaemon(final Profile prof) {
        super("/talk/daemon[started and not(code) and not(ended)]");
        this.profile = prof;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        new DeprecationNotice(this.profile).print(shell);
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        final int exit = new Script("end.sh").exec(xml);
        final Directives dirs = new Directives();
        if (exit == 0) {
            Logger.info(
                this, "the daemon is still running in %s (%s)",
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
     */
    private Iterable<Directive> end(final Shell shell,
        final String dir) throws IOException {
        final int exit = EndsDaemon.exit(shell, dir);
        final String stdout = new ShellCommand(
            shell,
            dir,
            "cat stdout"
        ).exec();
        final Collection<String> lines = Lists.newArrayList(
            Splitter.on(System.lineSeparator()).split(stdout)
        );
        final String highlights = Joiner.on("\n").join(
            Iterables.transform(
                Iterables.filter(
                    lines,
                    new Predicate<String>() {
                        @Override
                        public boolean apply(final String input) {
                            return input.startsWith(
                                EndsDaemon.HIGHLIGHTS_PREFIX
                            );
                        }
                    }
                ),
                new Function<String, String>() {
                    @Override
                    public String apply(final String str) {
                        return StringUtils.removeStart(
                            str, EndsDaemon.HIGHLIGHTS_PREFIX
                        );
                    }
                }
            )
        );
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
                    StringUtils.substring(
                        Joiner.on(System.lineSeparator()).join(
                            Iterables.skip(
                                lines,
                                Math.max(lines.size() - Tv.SIXTY, 0)
                            )
                        ),
                        -Tv.HUNDRED * Tv.THOUSAND
                    )
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

}
