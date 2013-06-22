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
package com.rultor.shell.ssh;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.File;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;

/**
 * RSA Private Key (for SSH).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "text")
@Loggable(Loggable.DEBUG)
public final class PrivateKey {

    /**
     * Content.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param txt Text
     */
    public PrivateKey(final String txt) {
        this.text = PrivateKey.normalize(txt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%d characters", this.text.length());
    }

    /**
     * Get file with private key.
     * @return The file
     * @throws IOException If some IO problem inside
     */
    public File asFile() throws IOException {
        final File file = File.createTempFile("delete-me-", ".pem");
        FileUtils.write(file, this.text, CharEncoding.UTF_8);
        FileUtils.forceDeleteOnExit(file);
        return file;
    }

    /**
     * Normalize key, if possible.
     * @param raw Raw text
     * @return Normalized text
     */
    private static String normalize(final String raw) {
        final String text = raw.replaceAll("\r", "")
            .replaceAll("\n\\s+|\n{2,}", "\n")
            .trim();
        Validate.isTrue(
            text.startsWith("-----BEGIN RSA PRIVATE KEY-----"),
            "Invalid start of an RSA private key"
        );
        Validate.isTrue(
            text.endsWith("-----END RSA PRIVATE KEY-----"),
            "Invalid end of an RSA private key"
        );
        return text;
    }

}
