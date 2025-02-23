/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
        if (xml == null || Manifests.exists(name) && !Manifests.read(name).startsWith("${")) {
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
