/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.shell.bash;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import com.rultor.shell.Batch;
import com.rultor.shell.Shells;
import com.rultor.shell.Terminal;
import com.rultor.snapshot.XemblyLine;
import com.rultor.tools.Vext;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.xembly.Directives;

/**
 * Incremental bash batch.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "shells", "commands" })
@Loggable(Loggable.DEBUG)
public final class IncrementalBash implements Batch {

    /**
     * Shells.
     */
    private final transient Shells shells;

    /**
     * Commands to execute.
     */
    private final transient Array<Vext> commands;

    /**
     * Public ctor.
     * @param shls Shells
     * @param cmds Commands
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public IncrementalBash(
        @NotNull(message = "shells can't be NULL") final Shells shls,
        @NotNull(message = "list of commands can't be NULL")
        final Collection<String> cmds) {
        this.shells = shls;
        final Collection<Vext> vexts = new ArrayList<Vext>(cmds.size());
        for (String cmd : cmds) {
            vexts.add(new Vext(cmd));
        }
        this.commands = new Array<Vext>(vexts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public int exec(
        @NotNull(message = "args can't be NULL") final Map<String, Object> args,
        @NotNull(message = "stream can't be NULL") final OutputStream output)
        throws IOException {
        return new Bash(this.shells, this.script(args)).exec(args, output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "bash batch of %d command(s) through %s",
            this.commands.size(), this.shells
        );
    }

    /**
     * Make a script.
     * @param args All args
     * @return Bash script
     */
    private String script(final Map<String, Object> args) {
        final StringBuilder script = new StringBuilder()
            .append("#set($dollar='$')")
            .append("set -o pipefail;\n")
            .append("set +o histexpand;\n")
            .append("ESCAPE=")
            .append(Terminal.escape(IncrementalBash.escape()))
            .append(';').append('\n');
        for (Vext cmd : this.commands) {
            script.append(this.script(args, cmd));
        }
        return script.toString();
    }

    /**
     * Make a script of one command.
     * @param args All args
     * @param cmd Command
     * @return Bash script
     */
    private String script(final Map<String, Object> args, final Vext cmd) {
        final String uid = String.format("bash-%d", System.nanoTime());
        final String velocity = StringUtils.strip(cmd.velocity(), " ;");
        final String command = cmd.print(args);
        return new StringBuilder()
            .append("echo; echo ${dollar} ")
            .append(Terminal.escape(command))
            .append(';').append('\n')
            .append(
                this.xembly(
                    new Directives()
                        .xpath("/snapshot")
                        .addIfAbsent("steps")
                        .add("step")
                        .attr("id", uid)
                        .add("start")
                        .set("`date  -u +%Y-%m-%dT%H:%M:%SZ`")
                )
            )
            .append(';').append('\n')
            .append(this.summary(uid, command))
            .append(";\nSTART=${dollar}(date +%s%N | tr -d N);\n")
            .append("STDERR=${dollar}(mktemp /tmp/bash-XXXX);\n")
            .append("{ ")
            .append(velocity)
            .append("; } 2> >( cat | eval $ESCAPE | tee ${dollar}STDERR );\n")
            .append("CODE=${dollar}?;\n")
            .append("FINISH=${dollar}(date +%s%N | tr -d N);\n")
            .append("if [ ${dollar}CODE = 0 ]; then\n  ")
            .append(
                this.xembly(
                    new Directives()
                        .xpath(this.xpath(uid))
                        // @checkstyle MultipleStringLiterals (1 line)
                        .add("level")
                        .set(Level.INFO.toString())
                )
            )
            .append(";\nelse\n  ")
            .append(
                this.xembly(
                    new Directives()
                        .xpath(this.xpath(uid))
                        .add("level")
                        .set(Level.SEVERE.toString())
                        .up()
                        .add("exception")
                        .add("cause")
                        .set("exit code ${dollar}{CODE}")
                        .up()
                        .add("stacktrace")
                        .set("${dollar}(tail -100 ${dollar}{STDERR})")
                )
            )
            .append(";\nfi;\n")
            .append(
                this.xembly(
                    new Directives()
                        .xpath(this.xpath(uid))
                        .add("finish")
                        .set("${dollar}(date -u +%Y-%m-%dT%H:%M:%SZ)")
                        .up()
                        .add("duration")
                        .set("${dollar}(((FINISH-START)/1000000))")
                )
            )
            .append(";\nrm -f ${dollar}{STDERR};\n")
            .append("if [ ${dollar}CODE != 0 ]; then\n  ")
            .append("exit ${dollar}CODE;\nfi;\n\n")
            .toString();
    }

    /**
     * Make xembly line.
     * @param dirs Directives
     * @return Bash command
     */
    private String xembly(final Directives dirs) {
        return String.format(
            "echo -e \"%s\"",
            new XemblyLine(dirs)
                .toString()
                // @checkstyle MultipleStringLiterals (2 lines)
                .replace("\\", "\\\\\\")
                .replace("\"", "\\\"")
                .replace(XemblyLine.MARK, this.escape(XemblyLine.MARK))
        );
    }

    /**
     * Print and escape summary in Xembly format.
     * @param uid Unique ID of the step
     * @param summary Summary to add to the step
     * @return Bash command
     */
    private String summary(final String uid, final String summary) {
        final String xembly = new XemblyLine(
            new Directives()
                .xpath(this.xpath(uid))
                .add("summary")
                .set(summary.replace("\\", "\\\\"))
        ).toString();
        return String.format(
            "echo -e '%s'",
            xembly.replace("'", "\\x27")
                .replace(XemblyLine.MARK, this.escape(XemblyLine.MARK))
        );
    }

    /**
     * Escape unicode chars for bash.
     * @param text Original text
     * @return Escaped
     */
    private String escape(final String text) {
        final StringBuilder out = new StringBuilder();
        for (byte chr : text.getBytes(Charsets.UTF_8)) {
            out.append("\\x").append(String.format("%X", chr));
        }
        return out.toString();
    }

    /**
     * XPath to find the step.
     * @param name Unique ID of the step
     * @return XPath for xembly
     */
    private String xpath(final String name) {
        return String.format("/snapshot/steps/step[@id='%s']", name);
    }

    /**
     * Escaper.
     * @return Bash script
     */
    @Cacheable(forever = true)
    private static String escape() {
        final ImmutableMap<String, String> pairs =
            new ImmutableMap.Builder<String, String>()
                .put("\\&", "&amp;")
                .put("'\"'\"'", "&apos;")
                .put("\"", "&quot;")
                .put("<", "&lt;")
                .put(">", "&gt;")
                .build();
        final StringBuilder script = new StringBuilder()
            .append("cat");
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            script.append(" | sed -e ':a' -e 'N' -e '$!ba' -e 's/")
                .append(pair.getKey())
                .append("/\\")
                .append(pair.getValue())
                .append("/g'");
        }
        return script.append(" | awk 1 ORS='&#10;'").toString();
    }

}
