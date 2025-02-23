/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import com.jcabi.aspects.Immutable;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;
import org.xembly.Xembler;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * YAML into XML.
 *
 * @since 1.0
 * @checkstyle AbbreviationAsWordInNameCheck (50 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "yaml")
final class YamlXML {

    /**
     * Yaml.
     */
    private final transient String yaml;

    /**
     * Ctor.
     * @param yml YAML
     */
    YamlXML(final String yml) {
        this.yaml = yml.trim();
    }

    /**
     * Get XML.
     * @return XML
     */
    public XML get() {
        final Yaml parser = new Yaml();
        final Directives dirs = new Directives().add("p");
        if (!this.yaml.isEmpty()) {
            try {
                dirs.append(YamlXML.dirs(parser.load(this.yaml)));
            } catch (final YAMLException ex) {
                throw new Profile.ConfigException(ex);
            }
        }
        return new XMLDocument(new Xembler(dirs).xmlQuietly());
    }

    /**
     * Convert something to dirs.
     * @param obj Object
     * @return Dirs
     */
    @SuppressWarnings("unchecked")
    private static Iterable<Directive> dirs(final Object obj) {
        final Directives dirs = new Directives();
        if (obj instanceof Map) {
            for (final Map.Entry<String, Object> ent
                : ((Map<String, Object>) obj).entrySet()) {
                dirs.add("entry")
                    .attr("key", ent.getKey())
                    .append(YamlXML.dirs(ent.getValue()))
                    .up();
            }
        } else if (obj instanceof List) {
            for (final Object item : (Iterable<Object>) obj) {
                dirs.add("item").append(YamlXML.dirs(item)).up();
            }
        } else if (obj == null) {
            dirs.set("");
        } else {
            dirs.set(obj.toString());
        }
        return dirs;
    }

}
