/**
 * Copyright (c) 2009-2016, rultor.com
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

import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Decorator allowing to add a deprecation message if and only if the
 * {@link Profile} is deprecated.
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.62
 */
public final class ProfileDeprecationAware implements Profile {

    /**
     * The underlying profile.
     */
    private final transient Profile profile;

    /**
     * Constructs a {@code ProfileDeprecationAware} with the specified
     * underlying {@code Profile}.
     * @param prof The underlying profile
     */
    public ProfileDeprecationAware(final Profile prof) {
        this.profile = prof;
    }

    @Override
    public String name() {
        return this.profile.name();
    }

    @Override
    public XML read() throws IOException {
        final XML xml = this.profile.read();
        this.check(xml);
        return xml;
    }

    @Override
    public Map<String, InputStream> assets() throws IOException {
        return this.profile.assets();
    }

    /**
     * Checks if the profile is deprecated and if so log the deprecation notice.
     * @throws IOException if it fails while getting the XML format of the
     *  profile
     */
    public void check() throws IOException {
        this.check(this.profile.read());
    }

    /**
     * Indicates whether the profile is deprecated or not.
     * @return True if the profile is deprecated, false otherwise
     * @throws IOException if it fails while getting the XML format of the
     *  profile
     */
    public boolean deprecated() throws IOException {
        return ProfileDeprecationAware.deprecated(this.profile.read());
    }

    /**
     * Checks if the profile is deprecated and if so log the deprecation notice.
     * @param prof The XML representation of the profile to check
     */
    private void check(final XML prof) {
        if (ProfileDeprecationAware.deprecated(prof)) {
            Logger.warn(this, "#### Deprecation Notice ####");
            Logger.warn(
                this,
                "You are using the Rultor default Docker image in your build."
            );
            Logger.warn(this, "The Rultor has to:");
            Logger.warn(
                this,
                "1. Provide the sudo package/command and not stop doing so "
            );
            Logger.warn(
                this,
                "whenever a change to the Dockerfile is made, even if Rultor "
            );
            Logger.warn(this, "itself does not need the sudo command.");
            Logger.warn(
                this,
                "2. Not install any gems to the global scope that interfere "
            );
            Logger.warn(this, "with pdd or est");
            Logger.warn(this, "########################");
        }
    }

    /**
     * Indicates whether the profile is deprecated or not.
     * @param prof The XML representation of the profile to test
     * @return True if the profile is deprecated, false otherwise
     */
    private static boolean deprecated(final XML prof) {
        final List<XML> images = prof.nodes(
            "/p/entry[@key='docker']/entry[@key='image']"
        );
        boolean deprecated = false;
        if (images.isEmpty()) {
            deprecated = true;
        } else {
            for (final XML image : images) {
                if ("yegor256/rultor".equals(image.node().getTextContent())) {
                    deprecated = true;
                    break;
                }
            }
        }
        return deprecated;
    }
}
