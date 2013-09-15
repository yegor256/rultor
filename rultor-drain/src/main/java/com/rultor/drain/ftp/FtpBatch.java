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
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Batch execution through FTP.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString(exclude = "password")
@EqualsAndHashCode(of = { "host", "login", "password", "port" })
@Loggable(Loggable.DEBUG)
final class FtpBatch {

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
     * Script to execute.
     */
    protected interface Script<T> {
        /**
         * Run it.
         * @param ftp FTP client
         * @return Result
         * @throws IOException If fails in IO operation
         */
        T exec(final FTPClient ftp) throws IOException;
    }

    /**
     * Public ctor.
     * @param hst FTP host name (or IP address)
     * @param user FTP user name for login
     * @param pwd FTP password
     * @param prt FTP port
     * @checkstyle ParameterNumber (4 lines)
     */
    protected FtpBatch(final String hst, final String user, final String pwd,
        final int prt) {
        this.host = hst;
        this.login = user;
        this.password = pwd;
        this.port = prt;
    }

    /**
     * Execute it.
     * @param script Script to execute
     * @param dir Directory to go to first
     * @return Result
     * @param <T> Type of result expected
     * @throws IOException If fails in IO operation
     */
    public <T> T exec(final FtpBatch.Script<T> script, final String dir)
        throws IOException {
        final FTPClient ftp = new FTPClient();
        ftp.setControlKeepAliveTimeout(TimeUnit.MINUTES.toSeconds(1));
        ftp.setRemoteVerificationEnabled(false);
        ftp.addProtocolCommandListener(
            new PrintCommandListener(
                new PrintStream(Logger.stream(Level.FINE, this))
            )
        );
        ftp.connect(this.host, this.port);
        try {
            final int reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new IOException(
                    String.format(
                        "failed to connect to %s:%d because of '%s'",
                        this.host, this.port,
                        ftp.getReplyString().trim()
                    )
                );
            }
            Logger.debug(
                this,
                "#exec(..): connected to ftp://%s:%d",
                this.host, this.port
            );
            if (!ftp.login(this.login, this.password)) {
                throw new IOException(
                    String.format(
                        "failed to login to %s:%d as '%s' because of '%s'",
                        this.host, this.port, this.login,
                        ftp.getReplyString().trim()
                    )
                );
            }
            Logger.debug(
                this,
                "#exec(..): authenticated as '%s'",
                this.login
            );
            try {
                ftp.enterLocalPassiveMode();
                this.chdir(ftp, dir);
                return script.exec(ftp);
            } finally {
                ftp.logout();
                Logger.debug(
                    this,
                    "#exec(..): logged out from ftp://%s:%d",
                    this.host, this.port
                );
            }
        } finally {
            ftp.disconnect();
            Logger.debug(
                this,
                "#exec(..): disconnected ftp://%s:%d",
                this.host, this.port
            );
        }
    }

    /**
     * CHDIR to the directory.
     * @param ftp FTP client
     * @param dir The directory
     * @throws IOException If IO problem inside
     */
    private void chdir(final FTPClient ftp, final String dir)
        throws IOException {
        for (String part : StringUtils.split(dir, '/')) {
            if (!this.exists(ftp, part) && !ftp.makeDirectory(part)) {
                throw new IOException(
                    String.format(
                        "failed to mkdir '%s' because of '%s'",
                        part,
                        ftp.getReplyString().trim()
                    )
                );
            }
            if (!ftp.changeWorkingDirectory(part)) {
                throw new IOException(
                    String.format(
                        "failed to change dir to '%s' because of '%s'",
                        part,
                        ftp.getReplyString().trim()
                    )
                );
            }
        }
        Logger.debug(this, "#chdir(..): changed current DIR to %s", dir);
    }

    /**
     * Directory exists?
     * @param ftp FTP client
     * @param dir The directory
     * @return TRUE if exists
     * @throws IOException If IO problem inside
     */
    private boolean exists(final FTPClient ftp, final String dir)
        throws IOException {
        boolean exists = false;
        final FTPFile[] files = ftp.listDirectories();
        final int reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            throw new IOException(
                String.format(
                    "failed to list directories because of '%s'",
                    ftp.getReplyString().trim()
                )
            );
        }
        for (FTPFile file : files) {
            if (file.getName().equals(dir)) {
                exists = true;
                break;
            }
        }
        Logger.debug(this, "#exists('%s'): %B", dir, exists);
        return exists;
    }

}
