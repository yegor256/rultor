/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.daemons;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.s3.Bucket;
import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.Time;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.TalkShells;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.NullInputStream;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Marks the daemon as done.
 *
 * @since 1.0
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
            String.join(
                "; ",
                String.format("if [ -d %s ]", Ssh.escape(dir)),
                String.format("then cd %s", Ssh.escape(dir)),
                "else echo 'Build directory is absent, internal error'",
                "exit",
                "fi",
                "if [ -r stdout ]",
                "then cat stdout | iconv -f utf-8 -t utf-8 -c | LANG=en_US.UTF-8 col -bx",
                "else echo 'Stdout not found, internal error'",
                "fi"
            ),
            new NullInputStream(0L),
            Files.newOutputStream(file.toPath()),
            Logger.stream(Level.WARNING, this)
        );
        new Shell.Empty(new Shell.Safe(shell)).exec(
            String.format("sudo rm -rf %1$s || rm -rf %s", Ssh.escape(dir))
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
        meta.setContentType("text/plain");
        meta.setContentEncoding(StandardCharsets.UTF_8.name());
        meta.setContentLength(file.length());
        final String key = String.format("%tY/%1$tm/%s.txt", new Date(), hash);
        this.bucket.ocket(key).write(Files.newInputStream(file.toPath()), meta);
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
            FileUtils.readLines(file, StandardCharsets.UTF_8).size()
        );
    }

}
