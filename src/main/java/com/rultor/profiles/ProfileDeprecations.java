/**
 * Copyright (c) 2009-2023 Yegor Bugayenko
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
package com.rultor.profiles;

import com.jcabi.ssh.Shell;
import com.jcabi.ssh.Ssh;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * Class allowing to print the deprecation notice if and only if the
 * underlying {@link Profile} is deprecated.
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.62
 */
public final class ProfileDeprecations {

    /**
     * The content of the notice.
     */
    private static final String CONTENT = StringUtils.join(
        "#### Deprecation Notice #### \n",
        "You are using the Rultor default Docker image in your build.",
        "The Rultor has to:\n",
        "1. Provide the sudo package/command and not stop doing so ",
        "whenever a change to the Dockerfile is made, even if Rultor ",
        "itself does not need the sudo command.\n",
        "2. Not install any gems to the global scope that interfere ",
        "with pdd or est\n",
        "#####################################\n"
    );

    /**
     * The underlying profile.
     */
    private final transient Profile profile;

    /**
     * Constructs a {@code ProfileDeprecations} with the specified underlying
     * {@code Profile}.
     * @param prof The underlying profile
     */
    public ProfileDeprecations(final Profile prof) {
        this.profile = prof;
    }

    /**
     * Prints the deprecation notice if the profile is deprecated.
     * @param shell The shell to use to print the deprecation notice if needed
     * @throws IOException if it fails while getting the XML format of the
     *  profile
     */
    public void print(final Shell shell) throws IOException {
        try {
            if (!ProfileDeprecations.empty(this.profile.read())) {
                ProfileDeprecations.output(ProfileDeprecations.CONTENT, shell);
            }
        } catch (final Profile.ConfigException ex) {
            ProfileDeprecations.output(ex.getMessage(), shell);
        }
    }

    /**
     * Indicates whether there is a deprecation notice or not.
     * @return True if there is no deprecation notice, false otherwise
     * @throws IOException if it fails while getting the XML format of the
     *  profile
     */
    public boolean empty() throws IOException {
        return ProfileDeprecations.empty(this.profile.read());
    }

    /**
     * Prints given message to a shell.
     * @param message Message to print to shell
     * @param shell Shell to print to
     * @throws IOException On failure to print to shell
     */
    private static void output(final String message, final Shell shell)
        throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Shell.Safe(shell).exec(
            String.format("echo -e \"%s\"", Ssh.escape(message)),
            new NullInputStream(0L),
            baos, baos
        );
    }

    /**
     * Indicates whether there is a deprecation notice or not.
     * @param prof The XML representation of the profile to test
     * @return True if there is no deprecation notice, false otherwise
     */
    private static boolean empty(final XML prof) {
        final List<XML> images = prof.nodes(
            "/p/entry[@key='docker']/entry[@key='image']"
        );
        final String def = "yegor256/rultor-image";
        boolean empty = true;
        if (images.isEmpty()) {
            empty = false;
        } else {
            for (final XML image : images) {
                if (def.equals(image.node().getTextContent())) {
                    empty = false;
                    break;
                }
            }
        }
        return empty;
    }
}
