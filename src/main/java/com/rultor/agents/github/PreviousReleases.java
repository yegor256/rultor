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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Release;
import com.jcabi.github.Releases;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

/**
 * Encapsulates a set of previous releases and a currently proposed release.
 *
 * @author Jimmy Spivey (JimDeanSpivey@gmail.com)
 * @version $Id$
 * @since 1.57
 */
@Immutable
public final class PreviousReleases {

    /**
     * The proposed tag.
     */
    private final transient Version version;

    /**
     * Previously released tags.
     */
    private final transient Releases priors;

    /**
     * Default constructor.
     * @param ver The proposed tag, wrapped in a Version object
     * @param previous A list of previously released tags.
     */
    public PreviousReleases(final Version ver, final Releases previous) {
        this.version = ver;
        this.priors = previous;
    }

    /**
     * Is the proposed tag true, with respect to previous released tags. For
     * example, if there are previous releases of [0.2,0.5,0.7], and the
     * proposed tag (version) is .6, this will return false. If the proposed
     * tag is .8 it will return true.
     * @return True if the release is valid
     */
    public boolean isValid() {
        final DefaultArtifactVersion latest = latest();
        return new DefaultArtifactVersion(this.version.toString())
            .compareTo(latest) == 1;
    }

    /**
     * Returns the latest release. If there are no releases, returns "0".
     * @return The latest release, otherwise "0" if there are no releases.
     */
    public DefaultArtifactVersion latest() {
        final DefaultArtifactVersion latest;
        final List<DefaultArtifactVersion> previous = this.transformVersions();
        if (previous.isEmpty()) {
            latest = new DefaultArtifactVersion("0");
        } else {
            latest = Collections.max(previous);
        }
        return latest;
    }

    /**
     * Transforms versionsFrom Release to DefaultArtificatVersion and filters
     * invalid version numbers. For example in these versions,
     * ["1.0", "2.0", "3.0-b"], "3.0-b" is just ignore, therefore version "2.0"
     * is the max.
     * @return All prior releases wrapped in a DefaultArtifactVersion
     */
    private List<DefaultArtifactVersion> transformVersions() {
        return FluentIterable
            .from(this.priors.iterate())
            .transform(new ReleaseToVersion())
            .filter(new ValidVersion())
            .toList();
    }

    private final class ValidVersion
        implements Predicate<DefaultArtifactVersion> {
        @Override
        public boolean apply(final DefaultArtifactVersion ver) {
            return new Version(ver.toString()).isValid();
        }
    }

    private final class ReleaseToVersion
        implements Function<Release, DefaultArtifactVersion> {
        @Override
        @SuppressWarnings("PMD.OnlyOneReturn")
        public DefaultArtifactVersion apply(final Release release) {
            final Release.Smart rel = new Release.Smart(release);
            try {
                final String tag = rel.tag();
                return new DefaultArtifactVersion(tag);
            } catch (final IOException exception) {
                Logger.error(this, "IOException caused from rel.tag()");
                return null;
            }
        }
    }
}
