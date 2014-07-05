/**
 * Copyright (c) 2009-2014, rultor.com
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
import com.jcabi.xml.XML;
import com.rultor.agents.TalkAgent;
import com.rultor.agents.shells.Shell;
import com.rultor.agents.shells.TalkShells;
import com.rultor.spi.Talk;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.logging.Level;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.CharEncoding;
import org.xembly.Directives;

/**
 * Marks the daemon as done.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public final class ArchivesDaemon extends TalkAgent.Abstract {

    /**
     * S3 bucket.
     */
    private final transient Bucket bucket;

    /**
     * Ctor.
     * @param bkt Bucket
     */
    public ArchivesDaemon(final Bucket bkt) {
        super("/talk/daemon[code and ended]");
        this.bucket = bkt;
    }

    @Override
    protected void process(final Talk talk, final XML xml) throws IOException {
        final Shell shell = new TalkShells().get(talk);
        final File file = File.createTempFile("rultor", ".log");
        final String dir = xml.xpath("/talk/daemon/dir/text()").get(0);
        new Shell.Safe(shell).exec(
            String.format("cat %s/stdout 2>&1", dir),
            new NullInputStream(0L),
            new FileOutputStream(file),
            Logger.stream(Level.WARNING, this)
        );
        new Shell.Empty(new Shell.Safe(shell)).exec(
            String.format("rm -rf %s", dir)
        );
        final String hash = xml.xpath("/talk/daemon/@id").get(0);
        final URI uri = this.upload(file, hash);
        talk.modify(
            new Directives().xpath("/talk/daemon").remove()
                .xpath("/talk").addIf("archive")
                .add("log").attr("id", hash)
                .set(uri.toString()),
            String.format("daemon archived into %s", uri)
        );
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
        FileUtils.deleteQuietly(file);
        return URI.create(String.format("s3://%s/%s", this.bucket.name(), key));
    }

}
