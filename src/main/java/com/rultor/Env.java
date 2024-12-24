/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor;

import com.jcabi.manifests.Manifests;
import com.jcabi.xml.XMLDocument;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;
import org.cactoos.text.UncheckedText;

/**
 * Environment variables either provided in the {@code MANIFEST.MF}
 * file or through shell variables.
 *
 * @since 1.50
 */
public final class Env {

    /**
     * Environment variable.
     */
    public static final String SETTINGS_XML = "SETTINGS_XML";

    /**
     * Private.
     */
    private Env() {
        // utility class
    }

    /**
     * Read one.
     * @param name The name of the variable
     * @return The value
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static String read(final String name) {
        final String xml = System.getenv(Env.SETTINGS_XML);
        final String ret;
        if (xml == null) {
            ret = Manifests.read(name);
        } else {
            final String res = "rultor-mf/MANIFEST.MF";
            final String manifest = new UncheckedText(
                new TextOf(new ResourceOf(res))
            ).asString();
            final Matcher matcher = Pattern.compile(
                String.format("%s: (\\$\\{[^}]+}|[^\\s]+)", name)
            ).matcher(manifest);
            if (!matcher.find()) {
                throw new IllegalArgumentException(
                    String.format("Can't find '%s' in %s:%n%s", name, res, manifest)
                );
            }
            final String prop = matcher.group(1);
            if (prop.startsWith("${")) {
                ret = new XMLDocument(xml).xpath(
                    String.format(
                        "/settings//*[name()='%s']/text()",
                        prop.substring(2, prop.length() - 1)
                    )
                ).get(0);
            } else {
                ret = prop;
            }
        }
        return ret;
    }

}
