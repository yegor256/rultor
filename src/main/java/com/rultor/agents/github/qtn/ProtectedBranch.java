/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.Pull;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.log.Logger;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Branch protection of the base branch of a pull request.
 *
 * <p>Before starting a merge, Rultor asks GitHub whether the base
 * branch is protected. A direct push of a merge commit to a protected
 * branch is rejected by GitHub, which used to surface only at the very
 * end of the build as a cryptic {@code git push} error. This check
 * makes such a failure happen early, with a human-readable
 * explanation. When the state of the branch can't be determined,
 * the merge proceeds as before.</p>
 *
 * @since 2.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "pull")
final class ProtectedBranch {

    /**
     * Message bundle.
     */
    private static final ResourceBundle PHRASES =
        ResourceBundle.getBundle("phrases");

    /**
     * Pull request.
     */
    private final transient Pull pull;

    /**
     * Ctor.
     *
     * @param ghpull Pull request to check
     */
    ProtectedBranch(final Pull ghpull) {
        this.pull = ghpull;
    }

    /**
     * Explanation of why the merge push will be rejected, if it will.
     *
     * @return Human-readable explanation, or empty string if the push
     *  should be attempted
     * @throws IOException If fails
     */
    String blocker() throws IOException {
        final Response res = this.fetch();
        return ProtectedBranch.blocker(
            res.status(),
            res.body(),
            this.pull.base().ref()
        );
    }

    /**
     * Explanation of why the merge push will be rejected, built from
     * the raw parts of the branch protection response.
     *
     * @param status HTTP status of the branch protection request
     * @param body HTTP body of the branch protection request
     * @param branch Base branch the merge would be pushed to
     * @return Human-readable explanation, or empty string if the push
     *  should be attempted
     */
    static String blocker(final int status, final String body,
        final String branch) {
        final String result;
        final String rules = ProtectedBranch.rules(status, body);
        if (rules.isEmpty()) {
            result = "";
        } else {
            result = String.format(
                ProtectedBranch.PHRASES.getString("QnMerge.branch-protected"),
                branch, rules
            );
        }
        return result;
    }

    /**
     * Names of the enabled branch protection rules.
     *
     * @param status HTTP status of the branch protection request
     * @param body HTTP body of the branch protection request
     * @return Comma-separated rule names, or empty string when the
     *  branch is not protected, or the state is unknown
     */
    static String rules(final int status, final String body) {
        final String result;
        if (status == HttpURLConnection.HTTP_OK) {
            result = ProtectedBranch.enabledRules(body);
        } else if (status == HttpURLConnection.HTTP_NOT_FOUND) {
            result = "";
        } else {
            Logger.warn(
                ProtectedBranch.class,
                "branch protection request answered with HTTP %d, proceeding without preflight",
                status
            );
            result = "";
        }
        return result;
    }

    private Response fetch() throws IOException {
        return this.pull.repo().github().entry().uri()
            .path("/repos")
            .path(this.pull.repo().coordinates().user())
            .path(this.pull.repo().coordinates().repo())
            .path("branches")
            .path(this.pull.base().ref())
            .path("protection")
            .back()
            .method(Request.GET)
            .fetch();
    }

    private static String enabledRules(final String body) {
        String result;
        try {
            final JsonObject json = Json.createReader(
                new StringReader(body)
            ).readObject();
            final Collection<String> rules = new ArrayList<>(0);
            if (ProtectedBranch.section(json, "required_status_checks")) {
                rules.add("required status checks");
            }
            if (ProtectedBranch.section(json, "required_pull_request_reviews")) {
                rules.add("required pull request reviews");
            }
            if (ProtectedBranch.flag(json, "enforce_admins")) {
                rules.add("restrictions enforced for administrators");
            }
            if (ProtectedBranch.flag(json, "required_signatures")) {
                rules.add("required commit signatures");
            }
            if (ProtectedBranch.flag(json, "required_linear_history")) {
                rules.add("required linear history");
            }
            if (ProtectedBranch.flag(json, "lock_branch")) {
                rules.add("the branch is locked");
            }
            result = String.join(", ", rules);
        } catch (final JsonException ex) {
            Logger.warn(
                ProtectedBranch.class,
                "branch protection body can't be parsed: %[exception]s",
                ex
            );
            result = "";
        }
        return result;
    }

    private static boolean section(final JsonObject json, final String name) {
        boolean present = false;
        final JsonValue value = json.get(name);
        if (value != null
            && value.getValueType() != JsonValue.ValueType.NULL) {
            present = true;
        }
        return present;
    }

    private static boolean flag(final JsonObject json, final String name) {
        boolean enabled = false;
        final JsonValue value = json.get(name);
        if (value instanceof JsonObject) {
            enabled = ((JsonObject) value).getBoolean("enabled", false);
        }
        return enabled;
    }
}
