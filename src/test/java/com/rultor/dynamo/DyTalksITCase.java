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
package com.rultor.dynamo;

import co.stateful.mock.MkSttc;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.manifests.Manifests;
import com.jcabi.matchers.XhtmlMatchers;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Integration case for {@link DyTalks}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
public final class DyTalksITCase {

    /**
     * DyTalks can add a talk.
     * @throws Exception If some problem inside
     */
    @Test
    public void addsTalks() throws Exception {
        final Talks talks = new DyTalks(
            this.dynamo(), new MkSttc().counters().get("")
        );
        final String name = "a5fe445";
        talks.create(name);
        MatcherAssert.assertThat(
            talks.get(name).read(),
            XhtmlMatchers.hasXPath("/talk")
        );
    }

    /**
     * DyTalks can list recent talks.
     * @throws Exception If some problem inside
     */
    @Test
    public void listsRecentTalks() throws Exception {
        final Talks talks = new DyTalks(
            this.dynamo(), new MkSttc().counters().get("")
        );
        final String name = "yegor256/rultor#529";
        talks.create(name);
        final Talk talk = talks.get(name);
        talk.active(false);
        MatcherAssert.assertThat(
            talks.recent().iterator().next().name(),
            Matchers.equalTo(name)
        );
    }

    /**
     * DynamoDB region for tests.
     * @return Region
     */
    private Region dynamo() {
        final String key = Manifests.read("Rultor-DynamoKey");
        MatcherAssert.assertThat(key.startsWith("AAAA"), Matchers.is(true));
        return new Region.Prefixed(
            new ReRegion(
                new Region.Simple(
                    new Credentials.Direct(
                        new Credentials.Simple(
                            key,
                            Manifests.read("Rultor-DynamoSecret")
                        ),
                        Integer.parseInt(System.getProperty("dynamo.port"))
                    )
                )
            ),
            "rt-"
        );
    }

}
