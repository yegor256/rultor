/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.log.Logger;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
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
import java.util.Objects;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.text.Joined;
import org.cactoos.text.Sub;
import org.cactoos.text.UncheckedText;

/**
 * Show current status.
 *
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
        Objects.requireNonNull(QnStatus.class.getResource("status.xsl"))
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
        lines.add(QnStatus.REPORT.applyTo(xml).trim());
        if (!xml.nodes("/talk[shell/host and daemon/dir]").isEmpty()) {
            final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
            final Shell.Plain shell = new Shell.Plain(
                new Shell.Safe(new TalkShells(xml).get())
            );
            lines.add(
                String.format(
                    " * Docker container ID: `%s...`",
                    new UncheckedText(
                        new Sub(
                            shell.exec(
                                String.format(
                                    // @checkstyle LineLength (1 line)
                                    "dir=%s; if [ -e \"${dir}/cid\" ]; then cat \"${dir}/cid\"; fi",
                                    Ssh.escape(dir)
                                )
                            ),
                            0, 20
                        )
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
                new UncheckedText(
                    new Joined("\n", lines)
                ).asString()
            )
        );
        Logger.info(this, "status request in #%d", comment.issue().number());
        return Req.DONE;
    }

}
