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
package com.rultor.drain.ftp;

import com.google.common.collect.Lists;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.spi.Drain;
import com.rultor.spi.Pulses;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 * Drain to an FTP file.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "batch", "file" })
@Loggable(Loggable.DEBUG)
public final class FileDrain implements Drain {

    /**
     * FTP batch.
     */
    private final transient FtpBatch batch;

    /**
     * File.
     */
    private final transient String file;

    /**
     * Public ctor.
     * @param hst FTP host name (or IP address)
     * @param user FTP user name for login
     * @param pwd FTP password
     * @param name FTP file name
     * @checkstyle ParameterNumber (5 lines)
     */
    public FileDrain(final String hst, final String user, final String pwd,
        final String name) {
        this(hst, user, pwd, FTP.DEFAULT_PORT, name);
    }

    /**
     * Public ctor.
     * @param host FTP host name (or IP address)
     * @param user FTP user name for login
     * @param pwd FTP password
     * @param port FTP port
     * @param name FTP file name
     * @checkstyle ParameterNumber (10 lines)
     */
    public FileDrain(
        @NotNull(message = "FTP host can't be NULL") final String host,
        @NotNull(message = "FTP user name can't be NULL") final String user,
        @NotNull(message = "FTP password can't be NULL") final String pwd,
        final int port,
        @NotNull(message = "FTP file name can't be NULL") final String name) {
        this.batch = new FtpBatch(host, user, pwd, port);
        this.file = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "FTP file '%s' in %s",
            this.file, this.batch
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pulses pulses() {
        return new Pulses.Array();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        this.batch.exec(
            new FtpBatch.Script<Void>() {
                @Override
                public Void exec(final FTPClient ftp) throws IOException {
                    FileDrain.this.append(ftp, lines);
                    return null;
                }
            },
            FilenameUtils.getFullPathNoEndSeparator(this.file)
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    "FileDrain: %s, file='%s'\n\n",
                    this.batch,
                    this.file
                )
            ),
            this.batch.exec(
                new FtpBatch.Script<InputStream>() {
                    @Override
                    public InputStream exec(final FTPClient ftp)
                        throws IOException {
                        return FileDrain.this.read(ftp);
                    }
                },
                FilenameUtils.getFullPathNoEndSeparator(this.file)
            )
        );
    }

    /**
     * Append lines to FTP.
     * @param ftp FTP client
     * @param lines Lines to append
     * @throws IOException If some I/O problem inside
     */
    private void append(final FTPClient ftp, final Iterable<String> lines)
        throws IOException {
        final String name = FilenameUtils.getBaseName(FileDrain.this.file);
        if (!ftp.appendFile(name, this.toStream(lines))) {
            throw new IOException(
                String.format(
                    "failed to append to %s in %s because of '%s'",
                    name, this.batch,
                    ftp.getReplyString().trim()
                )
            );
        }
        Logger.debug(
            this,
            "#append(..): appended some lines to '%s' in %s",
            this.file, this.batch
        );
    }

    /**
     * Read file from FTP.
     * @param ftp FTP client
     * @return Stream with content
     * @throws IOException If some I/O problem inside
     */
    private InputStream read(final FTPClient ftp) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String name = FilenameUtils.getBaseName(FileDrain.this.file);
        if (!ftp.retrieveFile(name, baos)) {
            throw new IOException(
                String.format(
                    "failed to read %s in %s because of '%s'",
                    name, this.batch,
                    ftp.getReplyString().trim()
                )
            );
        }
        Logger.debug(
            this,
            "#read(..): retrieved %d bytes from '%s' in %s",
            baos.toByteArray().length, this.file, this.batch
        );
        return new ByteArrayInputStream(baos.toByteArray());
    }

    /**
     * Convert lines to an input stream to append.
     * @param lines Lines to pack
     * @return Input stream
     */
    private InputStream toStream(final Iterable<String> lines) {
        return IOUtils.toInputStream(
            new StringBuilder(
                StringUtils.join(Lists.newLinkedList(lines), "\n")
            ).append('\n')
        );
    }

}
