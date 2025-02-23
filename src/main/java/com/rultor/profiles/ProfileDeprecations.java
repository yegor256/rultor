/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import org.cactoos.text.Joined;

/**
 * Class allowing to print the deprecation notice if and only if the
 * underlying {@link Profile} is deprecated.
 * @since 1.62
 */
public final class ProfileDeprecations {

    /**
     * The content of the notice.
     */
    private static final String CONTENT = new Joined(
        "",
        "#### Deprecation Notice #### \n",
        "You are using the Rultor default Docker image in your build.",
        "The Rultor has to:\n",
        "1. Provide the sudo package/command and not stop doing so ",
        "whenever a change to the Dockerfile is made, even if Rultor ",
        "itself does not need the sudo command.\n",
        "2. Not install any gems to the global scope that interfere ",
        "with pdd or est\n",
        "#####################################\n"
    ).toString();

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
