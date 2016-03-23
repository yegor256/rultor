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

import com.jcabi.xml.XMLDocument;
import com.rultor.spi.Profile;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests for ${@link ProfileDeprecations}.
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 * @version $Id$
 * @since 1.62
 */
public final class DeprecationNoticeTest {

    /**
     * The format of an profile that defines the docker image to use.
     */
    private static final String PROFILE_FORMAT = StringUtils.join(
        "<p><entry key='docker'>",
        "<entry key='image'>%s</entry>",
        "</entry></p>"
    );

    /**
     * ProfileDeprecations can identify a deprecated profile.
     * @throws Exception In case of error
     */
    @Test
    public void identifiesDeprecatedProfile() throws Exception {
        ProfileDeprecations deprecations = new ProfileDeprecations(
            new Profile.Fixed()
        );
        MatcherAssert.assertThat(deprecations.empty(), Matchers.is(false));
        deprecations = new ProfileDeprecations(
            new Profile.Fixed(
                new XMLDocument(
                    String.format(
                        DeprecationNoticeTest.PROFILE_FORMAT,
                        "yegor256/rultor"
                    )
                )
            )
        );
        MatcherAssert.assertThat(deprecations.empty(), Matchers.is(false));
    }

    /**
     * ProfileDeprecations can identify a valid profile.
     * @throws Exception In case of error
     */
    @Test
    public void identifiesValidProfile() throws Exception {
        final ProfileDeprecations deprecations = new ProfileDeprecations(
            new Profile.Fixed(
                new XMLDocument(
                    String.format(
                        DeprecationNoticeTest.PROFILE_FORMAT,
                        "foo"
                    )
                )
            )
        );
        MatcherAssert.assertThat(deprecations.empty(), Matchers.is(true));
    }
}
