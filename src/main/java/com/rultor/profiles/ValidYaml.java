/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * Valid YAML profile.
 *
 * @since 1.81
 * @checkstyle MultipleStringLiteralsCheck (100 lines)
 */
final class ValidYaml {

    /**
     * Commands that must be maps with scripts.
     */
    private static final Iterable<String> NAMES = Arrays.asList(
        "deploy",
        "merge",
        "release"
    );

    /**
     * YAML.
     */
    private final transient String yaml;

    /**
     * Ctor.
     * @param yml YAML
     */
    ValidYaml(final String yml) {
        this.yaml = yml.trim();
    }

    /**
     * Validation errors.
     * @return Errors, or empty list when YAML is valid
     */
    public List<String> errors() {
        final List<String> errors = new LinkedList<>();
        if (!this.yaml.isEmpty()) {
            this.check(errors);
        }
        return errors;
    }

    /**
     * Check YAML.
     * @param errors Error collector
     */
    private void check(final Collection<String> errors) {
        try {
            final Object data = new Yaml().load(this.yaml);
            if (data instanceof Map) {
                ValidYaml.commands((Map<?, ?>) data, errors);
            } else if (data != null) {
                errors.add("profile must be a map");
            }
        } catch (final YAMLException err) {
            errors.add(err.getMessage());
        }
    }

    /**
     * Check command sections.
     * @param config Profile map
     * @param errors Error collector
     */
    private static void commands(final Map<?, ?> config,
        final Collection<String> errors) {
        for (final String command : ValidYaml.NAMES) {
            ValidYaml.command(config, command, errors);
        }
    }

    /**
     * Check one command section.
     * @param config Profile map
     * @param name Command name
     * @param errors Error collector
     */
    private static void command(final Map<?, ?> config, final String name,
        final Collection<String> errors) {
        final Object section = config.get(name);
        if (section != null) {
            ValidYaml.section(name, section, errors);
        }
    }

    /**
     * Check present section.
     * @param name Section name
     * @param section Section value
     * @param errors Error collector
     */
    private static void section(final String name, final Object section,
        final Collection<String> errors) {
        if (section instanceof Map) {
            ValidYaml.mapped(name, (Map<?, ?>) section, errors);
        } else {
            errors.add(String.format("%s must be a map", name));
        }
    }

    /**
     * Check map section.
     * @param name Section name
     * @param section Section map
     * @param errors Error collector
     */
    private static void mapped(final String name, final Map<?, ?> section,
        final Collection<String> errors) {
        if (section.containsKey("script")) {
            ValidYaml.typed(name, section.get("script"), errors);
        } else {
            errors.add(String.format("%s.script is required", name));
        }
    }

    /**
     * Check script type.
     * @param name Section name
     * @param value Script value
     * @param errors Error collector
     */
    private static void typed(final String name, final Object value,
        final Collection<String> errors) {
        if (!ValidYaml.script(value)) {
            errors.add(
                String.format(
                    "%s.script must be a string or list of strings",
                    name
                )
            );
        }
    }

    /**
     * Check script value.
     * @param script Script value
     * @return True if it is a string or list of strings
     */
    private static boolean script(final Object script) {
        boolean valid = script instanceof String;
        if (script instanceof Iterable) {
            valid = true;
            for (final Object item : (Iterable<?>) script) {
                valid = valid && item instanceof String;
            }
        }
        return valid;
    }
}
