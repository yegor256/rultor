/**
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
package com.rultor.agents.github.qtn;

import com.jcabi.aspects.Immutable;
import com.jcabi.github.*;
import javax.json.JsonObject;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Checkable pull request.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @since 2.0
 */
@Immutable
@ToString
@EqualsAndHashCode
public class CheckablePull implements Pull {
    /**
     * Pull request.
     */
    private Pull pull;

    /**
     * Ctor.
     * @param pull Pull to validate
     */
    public CheckablePull(final Pull pull) {
        this.pull = pull;
    }
    @Override
    public Repo repo() {
        return pull.repo();
    }

    @Override
    public int number() {
        return pull.number();
    }

    @Override
    public PullRef base() throws IOException {
        return pull.base();
    }

    @Override
    public PullRef head() throws IOException {
        return pull.head();
    }

    @Override
    public Iterable<Commit> commits() throws IOException {
        return pull.commits();
    }

    @Override
    public Iterable<JsonObject> files() throws IOException {
        return pull.files();
    }

    @Override
    public void merge(String s) throws IOException {
        pull.merge(s);
    }

    @Override
    public MergeState merge(String s, String s1) throws IOException {
        return pull.merge(s, s1);
    }

    @Override
    public PullComments comments() throws IOException {
        return pull.comments();
    }

    @Override
    public Checks checks() throws IOException {
        return pull.checks();
    }

    @Override
    public void patch(JsonObject jsonObject) throws IOException {
        pull.patch(jsonObject);
    }

    @Override
    public JsonObject json() throws IOException {
        return pull.json();
    }

    @Override
    public int compareTo(Pull o) {
        return pull.compareTo(o);
    }

    /**
     * Checks if all checks are successful.
     * @return True if all checks are successful
     * @throws IOException If fails
     */
    public boolean allChecksSuccessful() throws IOException {
        boolean result = true;
        for (final Check check : pull.checks().all()) {
            if (!check.successful()) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Checks if file is affected by pull request.
     * @param fileName File name to check
     * @return True if all checks are successful
     * @throws IOException If fails
     */
    public boolean containsFile(String fileName) throws IOException {
        for (final JsonObject file : pull.files()) {
            if (file.getString("filename").equals(fileName)) {
                return true;
            }
        }
        return false;
    }
}
