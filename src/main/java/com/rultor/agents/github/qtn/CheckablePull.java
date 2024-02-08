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
import com.jcabi.github.Check;
import com.jcabi.github.Pull;
import java.io.IOException;
import javax.json.JsonObject;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Checkable pull request.
 *
 * @author Natalia Pozhidaeva (p.natasha.p@gmail.com)
 * @version $Id$
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
    public CheckablePull(final Pull ghpull) {
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
