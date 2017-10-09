/**
 * Copyright (c) 2009-2017, rultor.com
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
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Comment;
import com.jcabi.log.Logger;
import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.jcabi.xml.XSL;
import com.jcabi.xml.XSLDocument;
import com.rultor.agents.github.Answer;
import com.rultor.agents.github.Question;
import com.rultor.agents.github.Req;
import com.rultor.agents.shells.TalkShells;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.text.JoinedText;
import org.cactoos.text.SubText;

/**
 * Show current status.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.5
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "talk")
public final class QnStatus implements Question {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * XSL to generate report.
     */
    private static final XSL REPORT = XSLDocument.make(
        QnStatus.class.getResourceAsStream("status.xsl")
    );

    /**
     * Talk.
     */
    private final transient Talk talk;

    /**
     * Ctor.
     * @param tlk Talk
     */
    public QnStatus(final Talk tlk) {
        this.talk = tlk;
    }

    @Override
    public Req understand(final Comment.Smart comment,
        final URI home) throws IOException {
        final XML xml = this.talk.read();
        final Collection<String> lines = new LinkedList<>();
        if (!xml.nodes("/talk[shell/host and daemon/dir]").isEmpty()) {
            final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
            final Shell.Plain shell = new Shell.Plain(
                new Shell.Safe(new TalkShells(xml).get())
            );
            lines.add(
                String.format(
                    " * Docker container ID: `%s...`",
                    new SubText(
                        shell.exec(
                            String.format(
                                // @checkstyle LineLength (1 line)
                                "dir=%s; if [ -e \"${dir}/cid\" ]; then cat \"${dir}/cid\"; fi",
                                SSH.escape(dir)
                            )
                        ),
                        0, Tv.TWENTY
                    ).asString()
                )
            );
            lines.add(
                String.format(
                    " * working directory size: %s",
                    shell.exec(
                        String.format("du -hs \"%s\" | cut -f1", dir)
                    ).trim()
                )
            );
            lines.add(
                String.format(
                    " * server load average: %s",
                    shell.exec(
                        "uptime | awk '{print $12}' | cut -d ',' -f 1"
                    ).trim()
                )
            );
        }
        new Answer(comment).post(
            true,
            String.format(
                QnStatus.PHRASES.getString("QnStatus.response"),
                new JoinedText(
                    "\n",
                    QnStatus.REPORT.applyTo(xml).trim(),
                    lines.toString()
                ).asString()
            )
        );
        Logger.info(this, "status request in #%d", comment.issue().number());
        return Req.DONE;
    }

}
