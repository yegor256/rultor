/**
 * Copyright (c) 2009-2014, rultor.com
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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Content;
import com.jcabi.github.Repo;
import com.jcabi.xml.XML;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.CharEncoding;

/**
 * Github Profile.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "repo")
final class GithubProfile implements Profile {

    /**
     * File name.
     */
    private static final String FILE = ".rultor.yml";

    /**
     * Repo.
     */
    private final transient Repo repo;

    /**
     * Ctor.
     * @param rpo Repo
     */
    GithubProfile(final Repo rpo) {
        this.repo = rpo;
    }

    @Override
    public XML read() throws IOException {
        return new YamlXML(this.yml()).get();
    }

    @Override
    public Map<String, InputStream> assets() throws IOException {
        return new HashMap<String, InputStream>(0);
    }

    /**
     * Get .rultor.yml file.
     * @return Its content
     */
    @Cacheable
    private String yml() throws IOException {
        final boolean exists = Iterables.any(
            this.repo.contents().iterate("", ""),
            new Predicate<Content>() {
                @Override
                public boolean apply(final Content input) {
                    return input.path().equals(GithubProfile.FILE);
                }
            }
        );
        final String yml;
        if (exists) {
            yml = new String(
                Base64.decodeBase64(
                    new Content.Smart(
                        this.repo.contents().get(GithubProfile.FILE)
                    ).content()
                ),
                CharEncoding.UTF_8
            );
        } else {
            yml = "";
        }
        return yml;
    }

}
