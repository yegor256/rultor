/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Check;
import com.jcabi.github.Pull;
import jakarta.json.JsonObject;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Checkable pull request.
 *
 * @since 2.0
 */
@Immutable
@ToString
@EqualsAndHashCode
final class CheckablePull {
    /**
     * Pull request.
     */
    private final transient Pull pull;

    /**
     * Ctor.
     * @param ghpull Pull to validate
     */
    CheckablePull(final Pull ghpull) {
        this.pull = ghpull;
    }

    /**
     * Checks if all checks are successful.
     * @return True if all checks are successful
     * @throws IOException If fails
     */
    public boolean allChecksSuccessful() throws IOException {
        boolean result = true;
        for (final Check check : this.pull.checks().all()) {
            if (!check.successful()) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Checks if file is affected by pull request.
     * @param file File name to check
     * @return True if all checks are successful
     * @throws IOException If fails
     */
    public boolean containsFile(final String file) throws IOException {
        boolean result = false;
        for (final JsonObject pullfile : this.pull.files()) {
            if (pullfile.getString("filename").equalsIgnoreCase(file)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
