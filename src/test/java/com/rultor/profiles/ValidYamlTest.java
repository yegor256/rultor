/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.profiles;

import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ValidYaml}.
 *
 * @since 1.81
 * @checkstyle MultipleStringLiteralsCheck (100 lines)
 */
final class ValidYamlTest {

    /**
     * Script property.
     */
    private static final String SCRIPT = " script:";

    /**
     * Script command.
     */
    private static final String PWD = "  - pwd";

    /**
     * ValidYaml accepts empty YAML.
     */
    @Test
    void acceptsEmptyYaml() {
        MatcherAssert.assertThat(
            "Empty YAML should be valid",
            new ValidYaml("").errors(),
            Matchers.empty()
        );
    }

    /**
     * ValidYaml rejects YAML with missing script in merge command.
     */
    @Test
    void rejectsYamlWithoutMergeScript() {
        MatcherAssert.assertThat(
            "Merge command without script should be rejected",
            ValidYamlTest.errors("merge:", ValidYamlTest.PWD),
            Matchers.hasItem(Matchers.containsString("merge"))
        );
    }

    /**
     * ValidYaml rejects YAML with missing script in deploy command.
     */
    @Test
    void rejectsYamlWithoutDeployScript() {
        MatcherAssert.assertThat(
            "Deploy command without script should be rejected",
            ValidYamlTest.errors("deploy:", ValidYamlTest.PWD),
            Matchers.hasItem(Matchers.containsString("deploy"))
        );
    }

    /**
     * ValidYaml rejects YAML with missing script in release command.
     */
    @Test
    void rejectsYamlWithoutReleaseScript() {
        MatcherAssert.assertThat(
            "Release command without script should be rejected",
            ValidYamlTest.errors("release:", ValidYamlTest.PWD),
            Matchers.hasItem(Matchers.containsString("release"))
        );
    }

    /**
     * ValidYaml accepts YAML with script in merge command.
     */
    @Test
    void acceptsYamlWithOnlyMerge() {
        MatcherAssert.assertThat(
            "Merge command with script should be valid",
            ValidYamlTest.errors(
                "merge:",
                ValidYamlTest.SCRIPT,
                ValidYamlTest.PWD
            ),
            Matchers.empty()
        );
    }

    /**
     * ValidYaml accepts YAML with script in release command.
     */
    @Test
    void acceptsYamlWithOnlyRelease() {
        MatcherAssert.assertThat(
            "Release command with script should be valid",
            ValidYamlTest.errors(
                "release:",
                ValidYamlTest.SCRIPT,
                ValidYamlTest.PWD
            ),
            Matchers.empty()
        );
    }

    /**
     * ValidYaml accepts YAML with script in deploy command.
     */
    @Test
    void acceptsYamlWithOnlyDeploy() {
        MatcherAssert.assertThat(
            "Deploy command with script should be valid",
            ValidYamlTest.errors(
                "deploy:",
                ValidYamlTest.SCRIPT,
                ValidYamlTest.PWD
            ),
            Matchers.empty()
        );
    }

    /**
     * ValidYaml accepts YAML with script in all commands.
     */
    @Test
    void acceptsYamlWithAllCommands() {
        MatcherAssert.assertThat(
            "Profile with all command scripts should be valid",
            ValidYamlTest.errors(
                "deploy:",
                ValidYamlTest.SCRIPT,
                ValidYamlTest.PWD,
                "release:",
                ValidYamlTest.SCRIPT,
                ValidYamlTest.PWD,
                "merge:",
                ValidYamlTest.SCRIPT,
                ValidYamlTest.PWD
            ),
            Matchers.empty()
        );
    }

    /**
     * Build errors.
     * @param lines YAML lines
     * @return Errors
     */
    private static List<String> errors(final String... lines) {
        return new ValidYaml(
            String.join("\n", lines)
        ).errors();
    }
}
