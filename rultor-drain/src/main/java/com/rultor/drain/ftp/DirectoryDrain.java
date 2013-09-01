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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.spi.Drain;
import com.rultor.spi.Pageable;
import com.rultor.spi.Work;
import com.rultor.tools.Time;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Drain in an FTP directory.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "work", "host", "login", "password", "port", "dir" })
@Loggable(Loggable.DEBUG)
public final class DirectoryDrain implements Drain {

    /**
     * The work it is busy with at the moment.
     */
    private final transient Work work;

    /**
     * Host name.
     */
    private final transient String host;

    /**
     * Login.
     */
    private final transient String login;

    /**
     * Password.
     */
    private final transient String password;

    /**
     * Port.
     */
    private final transient int port;

    /**
     * Directory name.
     */
    private final transient String dir;

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param hst FTP host name (or IP address)
     * @param user FTP user name for login
     * @param pwd FTP password
     * @param name FTP directory name
     * @checkstyle ParameterNumber (10 lines)
     */
    public DirectoryDrain(final Work wrk, final String hst, final String user,
        final String pwd, final String name) {
        this(wrk, hst, user, pwd, FTP.DEFAULT_PORT, name);
    }

    /**
     * Public ctor.
     * @param wrk Work we're in
     * @param hst FTP host name (or IP address)
     * @param user FTP user name for login
     * @param pwd FTP password
     * @param prt FTP port
     * @param name FTP directory name
     * @checkstyle ParameterNumber (10 lines)
     */
    public DirectoryDrain(
        @NotNull(message = "work can't be NULL") final Work wrk,
        @NotNull(message = "FTP host can't be NULL") final String hst,
        @NotNull(message = "FTP user name can't be NULL") final String user,
        @NotNull(message = "FTP password can't be NULL") final String pwd,
        final int prt,
        @NotNull(message = "FTP directory can't be NULL") final String name) {
        this.work = wrk;
        this.host = hst;
        this.login = user;
        this.password = pwd;
        this.port = prt;
        this.dir = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format(
            "files in ftp://%s@%s:%d/%s/%s",
            this.login, this.host, this.port, this.dir, this.prefix()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Time, Time> pulses() throws IOException {
        return new Pageable.Array<Time>(
            new FtpBatch(this.host, this.login, this.password, this.port).exec(
                new FtpBatch.Script<Collection<Time>>() {
                    @Override
                    public Collection<Time> exec(final FTPClient ftp)
                        throws IOException {
                        return DirectoryDrain.this.pulses(ftp);
                    }
                },
                this.prefix()
            )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void append(final Iterable<String> lines) throws IOException {
        this.file().append(lines);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read() throws IOException {
        return new SequenceInputStream(
            IOUtils.toInputStream(
                String.format(
                    "DirectoryDrain: work='%s', ftp='%s@%s:%d'\n",
                    this.work, this.login, this.host, this.port
                ),
                CharEncoding.UTF_8
            ),
            this.file().read()
        );
    }

    /**
     * Fetch all times.
     * @param ftp FTP client
     * @return Collection of times in the directory
     * @throws IOException If IO problem
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Collection<Time> pulses(final FTPClient ftp) throws IOException {
        final FTPFile[] files = ftp.listFiles();
        final Collection<Time> times = new ArrayList<Time>(files.length);
        final int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            throw new IOException(
                String.format(
                    "failed to list files because of '%s'",
                    ftp.getReplyString().trim()
                )
            );
        }
        for (FTPFile file : files) {
            if (!file.getName().matches("\\d{20}")) {
                continue;
            }
            times.add(new Time(Long.parseLong(file.getName())));
        }
        return times;
    }

    /**
     * Make file drain.
     * @return File drain
     */
    private FileDrain file() {
        return new FileDrain(
            this.host,
            this.login,
            this.password,
            this.port,
            String.format(
                "%s/%020d",
                this.prefix(),
                this.work.scheduled().millis()
            )
        );
    }

    /**
     * Make a prefix.
     * @return Prefix to use
     */
    private String prefix() {
        return String.format(
            "%s/%s/%s",
            this.dir,
            this.work.owner(),
            this.work.rule()
        );
    }

}
