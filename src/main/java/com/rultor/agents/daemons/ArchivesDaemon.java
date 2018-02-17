/**
 * Copyright (c) 2009-2018, rultor.com
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

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.s3.Bucket;
import com.jcabi.ssh.SSH;
import com.jcabi.ssh.Shell;
import com.jcabi.xml.XML;
import com.rultor.Time;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import javax.ws.rs.core.MediaType;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.CharEncoding;
import org.cactoos.text.JoinedText;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Marks the daemon as done.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = "bucket")
public final class ArchivesDaemon extends AbstractAgent {

    /**
     * S3 bucket.
     */
    private final transient Bucket bucket;

    /**
     * Ctor.
     * @param bkt Bucket
     */
    public ArchivesDaemon(final Bucket bkt) {
        super(
            "/talk/daemon[started and code and ended and dir]",
            "/talk/shell"
        );
        this.bucket = bkt;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Shell shell = new TalkShells(xml).get();
        final File file = File.createTempFile("rultor", ".log");
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        new Shell.Safe(shell).exec(
            new JoinedText(
                "; ",
                String.format("if [ -d %s ]", SSH.escape(dir)),
                String.format("then cd %s", SSH.escape(dir)),
                "else echo 'Build directory is absent, internal error'",
                "exit",
                // @checkstyle MultipleStringLiteralsCheck (1 line)
                "fi",
                "if [ -r stdout ]",
                // @checkstyle LineLength (1 line)
                "then cat stdout | iconv -f utf-8 -t utf-8 -c | LANG=en_US.UTF-8 col -bx",
                "else echo 'Stdout not found, internal error'",
                "fi"
            ).asString(),
            new NullInputStream(0L),
            new FileOutputStream(file),
            Logger.stream(Level.WARNING, this)
        );
        new Shell.Empty(new Shell.Safe(shell)).exec(
            String.format("sudo rm -rf %1$s || rm -rf %s", SSH.escape(dir))
        );
        final String hash = xml.xpath("/talk/daemon/@id").get(0);
        final URI uri = this.upload(file, hash);
        final String title = ArchivesDaemon.title(xml, file);
        Logger.info(
            this, "daemon of %s archived into %s: %s",
            xml.xpath("/talk/@name").get(0), uri, title
        );
        FileUtils.deleteQuietly(file);
        return new Directives().xpath("/talk/daemon").remove()
            .xpath("/talk").addIf("archive")
            .add("log").attr("id", hash)
            .attr("title", title)
            .set(uri.toString());
    }

    /**
     * Upload file to S3.
     * @param file The file
     * @param hash Hash
     * @return S3 URI
     * @throws IOException If fails
     */
    private URI upload(final File file, final String hash) throws IOException {
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(MediaType.TEXT_PLAIN);
        meta.setContentEncoding(CharEncoding.UTF_8);
        meta.setContentLength(file.length());
        final String key = String.format("%tY/%1$tm/%s.txt", new Date(), hash);
        this.bucket.ocket(key).write(new FileInputStream(file), meta);
        return URI.create(String.format("s3://%s/%s", this.bucket.name(), key));
    }

    /**
     * Make a title.
     * @param xml XML
     * @param file File with stdout
     * @return Title
     * @throws IOException If fails
     */
    private static String title(final XML xml, final File file)
        throws IOException {
        final int code = Integer.parseInt(
            xml.xpath("/talk/daemon/code/text()").get(0)
        );
        final String status;
        if (code == 0) {
            status = "SUCCESS";
        } else {
            status = "FAILURE";
        }
        return Logger.format(
            "%s: %d (%s) in %[ms]s, %d lines",
            xml.xpath("/talk/daemon/title/text()").get(0),
            code,
            status,
            new Time(xml.xpath("/talk/daemon/ended/text()").get(0)).msec()
            - new Time(xml.xpath("/talk/daemon/started/text()").get(0)).msec(),
            FileUtils.readLines(file).size()
        );
    }

}
