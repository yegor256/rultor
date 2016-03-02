/**
 * Copyright (c) 2009-2016, rultor.com
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
import com.jcabi.github.Release;
import com.jcabi.github.Repo;
import com.jcabi.github.Smarts;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Release Tag validator, ensures not releasing already outdated tags.
 *
 * @author Armin Braun (me@obrown.io)
 * @version $Id$
 * @since 1.62
 */
@Immutable
final class ReleaseTag {

    /**
     * Pattern to match semantically valid versions, that consist only of
     * digits and dots.
     */
    private static final Pattern VERSION_PATTERN =
        Pattern.compile("^(\\d+\\.)*(\\d+)$");

    /**
     * Github.
     */
    private final transient Repo repo;

    /**
     * Tag-name of the release.
     */
    private final transient String name;

    /**
     * Ctor.
     * @param rpo Github repo
     * @param version String release tag name
     */
    ReleaseTag(final Repo rpo, final String version) {
        this.repo = rpo;
        this.name = version;
    }

    /**
     * Checks if this tag can be released.
     * A tag can be released if it is either not named as a semantically
     * correct version or has a higher version number than all existing tags.
     * @return True if this tag can be released
     * @throws IOException on error
     */
    public boolean allowed() throws IOException {
        return !ReleaseTag.valid(this.name)
            || ReleaseTag.newer(this.reference(), this.name);
    }

    /**
     * Returns the tag name of the highest version from the repo.
     * @return String name of the highest released version
     * @throws IOException on error
     */
    public String reference() throws IOException {
        String tag = "0";
        final Iterable<Release.Smart> rels =
            new Smarts<>(this.repo.releases().iterate());
        for (final Release.Smart rel : rels) {
            final String version = rel.tag();
            if (ReleaseTag.valid(version) && ReleaseTag.newer(tag, version)) {
                tag = version;
            }
        }
        return tag;
    }

    /**
     * Checks that a tag is newer than a given reference.
     * @param reference String
     * @param tag String
     * @return True if tag is newer than reference
     */
    private static boolean newer(final String reference, final String tag) {
        return new DefaultArtifactVersion(reference).compareTo(
            new DefaultArtifactVersion(tag)
        ) < 0;
    }

    /**
     * Checks that tag is a valid release version, consisting only in digits
     * and dots.
     * @param identifier String tag name
     * @return True if identifier is a valid release version
     */
    private static boolean valid(final String identifier) {
        return ReleaseTag.VERSION_PATTERN.matcher(identifier).matches();
    }

}
