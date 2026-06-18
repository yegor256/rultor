/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Check;
import com.jcabi.github.Pull;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.util.Set;
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
     * GitHub `mergeable_state` values that prevent the merge from going
     * through at the `git push` step.
     */
    private static final Set<String> BLOCKING_STATES = Set.of(
        "blocked", "dirty", "behind", "draft"
    );

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
            if (!check.successful() && !check.skipped()) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Checks the GitHub `mergeable_state` of the pull request.
     * Returns `true` when the state is one the merge cannot recover
     * from at the `git push` step: branch protection rule violation,
     * unresolved review conversation, out of date branch, draft, or
     * conflict with the base. Unknown or absent state is treated as
     * not blocking to preserve the previous behavior on hosts that
     * do not return the field.
     * @return True when GitHub will reject a `git push` for this PR
     * @throws IOException If fails
     */
    public boolean blocked() throws IOException {
        final JsonObject json = this.pull.json();
        final boolean result;
        if (!json.containsKey("mergeable_state")
            || json.isNull("mergeable_state")
            || json.get("mergeable_state").getValueType() != JsonValue.ValueType.STRING) {
            result = false;
        } else {
            result = CheckablePull.BLOCKING_STATES.contains(
                json.getString("mergeable_state")
            );
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
