/**
 * Copyright (c) 2009-2022 Yegor Bugayenko
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
package com.rultor.agents.req;

import com.jcabi.aspects.Immutable;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Decrypt.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 1.37.4
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "profile")
final class Decrypt {

    /**
     * Space delimiter.
     */
    private static final String SPACE = " ";

    /**
     * Profile.
     */
    private final transient Profile profile;

    /**
     * Proxy usage command string.
     */
    private final transient String proxy;

    /**
     * Ctor.
     * @param prof Profile
     * @param host Host
     * @param port Port
     */
    Decrypt(final Profile prof, final String host, final int port) {
        this.profile = prof;
        if (host.isEmpty()) {
            this.proxy = "";
        } else {
            this.proxy = String.format("http-proxy=%s:%d", host, port);
        }
    }

    /**
     * Ctor.
     * @param prof Profile
     */
    Decrypt(final Profile prof) {
        this(prof, "", 0);
    }

    /**
     * Decrypt instructions.
     * @return Instructions
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Iterable<String> commands() throws IOException {
        final Collection<XML> assets =
            this.profile.read().nodes("/p/entry[@key='decrypt']/entry");
        final Collection<String> commands = new LinkedList<>();
        if (!assets.isEmpty()) {
            commands.add("gpgconf --reload gpg-agent");
            commands.add(
                String.join(
                    Decrypt.SPACE,
                    "gpg --keyserver keyserver.ubuntu.com",
                    this.proxy,
                    "--verbose --recv-keys 3FD3FA7E9AF0FA4C"
                )
            );
            commands.add("gpg --version");
            commands.add("gpg --list-keys");
        }
        for (final XML asset : assets) {
            final String key = asset.xpath("@key").get(0);
            final String enc = String.format("%s.enc", key);
            commands.add(
                String.join(
                    Decrypt.SPACE,
                    "gpg --verbose",
                    "\"--secret-keyring=$(pwd)/.gnupg/secring.gpg\"",
                    String.format(
                        "--decrypt %s > %s",
                        Ssh.escape(asset.xpath("./text()").get(0)),
                        Ssh.escape(enc)
                    )
                )
            );
            commands.add(
                String.format(
                    String.join(
                        Decrypt.SPACE,
                        "gpg --no-tty --batch --verbose --decrypt",
                        "--ignore-mdc-error",
                        "--passphrase %s %s > %s"
                    ),
                    Ssh.escape(
                        String.format("rultor-key:%s", this.profile.name())
                    ),
                    Ssh.escape(enc),
                    Ssh.escape(key)
                )
            );
            commands.add(String.format("rm -rf %s", Ssh.escape(enc)));
        }
        commands.add("rm -rf .gnupg");
        return commands;
    }
}
