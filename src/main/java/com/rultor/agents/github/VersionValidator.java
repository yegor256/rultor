/**
 * Copyright (c) 2009-2015, rultor.com
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
package com.rultor.agents.github;

import com.jcabi.aspects.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Various validations regarding version numbers.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.57
 */
@Immutable
public final class VersionValidator {

    /**
     * Version pattern.
     */
    private static final Pattern VERSION_PATTERN =
            Pattern.compile("\\.?(?:\\d+\\.)*\\d+");

    /**
     * Singleton instance of Version Validator.
     */
    private static final VersionValidator VERSION_VALIDATOR =
            new VersionValidator();

    /**
     * Force clients to use factory method.
     */
    private VersionValidator() {
    }

    /**
     * Factory method to get an instance of VersionValidator. All operations
     * are already thread safe.
     * @return A singleton instance of VersionValidator
     */
    public static VersionValidator getInstance() {
        return VERSION_VALIDATOR;
    }

    /**
     * Valid version numbers:
     * .1
     * 2.2
     * .1.2
     * 1.2.3.4.5.6.7
     *
     * Invalid version numbers:
     * abc
     * a.b.c
     * 1.
     * 1.2.
     * @param version Version number from a release
     * @return True if the version is valid, false otherwise
     */
    public boolean isVersionValid(final String version) {
        final Matcher matcher = VERSION_PATTERN.matcher(version);
        return matcher.matches();
    }

    /**
     * Is this tagged release valid.
     * @param tag The release to be tagged
     * @param previous The previous releases
     * @return True if the release is valid
     */
    public boolean isReleaseValid(final String tag,
                                  final List<DefaultArtifactVersion> previous) {
        final DefaultArtifactVersion max;
        if (previous.isEmpty()) {
            max = new DefaultArtifactVersion("0");
        } else {
            max = Collections.max(previous);
        }
        return new DefaultArtifactVersion(tag).compareTo(max) == 1;
    }
}
