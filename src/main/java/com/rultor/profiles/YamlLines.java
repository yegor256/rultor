/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;

/**
 * Lines of entries in a YAML document.
 *
 * <p>The document is composed into a tree of nodes and the path of
 * every entry, e.g. <tt>"/merge/script/0"</tt>, is mapped to the number
 * of the line where this entry starts at, one-based. Paths are compatible
 * with instance locations reported by the JSON schema validator, so
 * validation errors can point to exact lines of {@code .rultor.yml}.</p>
 *
 * @since 2.1
 */
final class YamlLines {

    /**
     * YAML document.
     */
    private final transient String yml;

    /**
     * Ctor.
     *
     * @param yml YAML document
     */
    YamlLines(final String yml) {
        this.yml = yml;
    }

    /**
     * Path of every entry to the line it starts at.
     *
     * @return Lines of entries, empty map for an empty document
     */
    Map<String, Integer> lines() {
        final Map<String, Integer> lines = new HashMap<>(0);
        final Node root = new Yaml().compose(new StringReader(this.yml));
        if (root != null) {
            YamlLines.walk(
                root,
                "",
                YamlLines.line(root),
                lines,
                Collections.newSetFromMap(new IdentityHashMap<>())
            );
        }
        return lines;
    }

    private static void walk(final Node node, final String path,
        final int line, final Map<String, Integer> lines,
        final Set<Node> seen) {
        if (seen.add(node)) {
            lines.put(path, line);
            if (node instanceof MappingNode) {
                for (final NodeTuple tuple
                    : ((MappingNode) node).getValue()) {
                    final Node key = tuple.getKeyNode();
                    YamlLines.walk(
                        tuple.getValueNode(),
                        String.format("%s/%s", path, YamlLines.key(key)),
                        YamlLines.line(key),
                        lines,
                        seen
                    );
                }
            } else if (node instanceof SequenceNode) {
                final List<Node> items = ((SequenceNode) node).getValue();
                for (int index = 0; index < items.size(); ++index) {
                    YamlLines.walk(
                        items.get(index),
                        String.format("%s/%d", path, index),
                        YamlLines.line(items.get(index)),
                        lines,
                        seen
                    );
                }
            }
        }
    }

    private static String key(final Node key) {
        final String name;
        if (key instanceof ScalarNode) {
            name = ((ScalarNode) key).getValue();
        } else {
            name = key.getTag().getValue();
        }
        return name;
    }

    private static int line(final Node node) {
        final Mark mark = node.getStartMark();
        final int line;
        if (mark == null) {
            line = 0;
        } else {
            line = mark.getLine() + 1;
        }
        return line;
    }
}
