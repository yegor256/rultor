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
package com.rultor.profiles;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.github.Coordinates;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.github.wire.RetryCarefulWire;
import com.jcabi.manifests.Manifests;
import com.jcabi.xml.XML;
import com.rultor.agents.github.TalkIssues;
import com.rultor.spi.Profile;
import com.rultor.spi.Talk;
import java.io.IOException;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Profiles.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
public final class Profiles {

    /**
     * Fetch a profile from a talk.
     * @param talk The talk
     * @return Profile found
     * @throws IOException If fails
     */
    public Profile fetch(final Talk talk) throws IOException {
        final Profile profile;
        if (Talk.TEST_NAME.equals(talk.name())) {
            profile = new Profile.Fixed();
        } else {
            final XML xml = talk.read();
            if (xml.nodes("/talk/wire").isEmpty()) {
                profile = Profile.EMPTY;
            } else {
                profile = Profiles.fetch(xml);
            }
        }
        return profile;
    }

    /**
     * Fetch a profile from an XML.
     * @param xml The XML
     * @return Profile found
     */
    private static Profile fetch(final XML xml) {
        final Profile profile;
        final List<String> type = xml.xpath("//request/type/text()");
        if (type.isEmpty() || !"merge".equals(type.get(0))) {
            profile = new GithubProfile(
                new TalkIssues(Profiles.github(), xml).get().repo()
            );
        } else {
            profile = new GithubProfile(
                Profiles.github().repos().get(
                    new Coordinates.Simple(
                        xml.xpath(
                            "//request/args/arg[@name='fork']/text()"
                        ).get(0)
                    )
                ),
                xml.xpath(
                    "//request/args/arg[@name='fork_branch']/text()"
                ).get(0)
            );
        }
        return profile;
    }

    /**
     * Make github.
     * @return Github
     */
    @Cacheable(forever = true)
    private static Github github() {
        return new RtGithub(
            new RtGithub(
                Manifests.read("Rultor-GithubToken")
            ).entry().through(
                RetryCarefulWire.class,
                Tv.HUNDRED
            )
        );
    }

}
