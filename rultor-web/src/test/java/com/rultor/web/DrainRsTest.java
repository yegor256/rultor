/**
 * Copyright (c) 2009-2013, rultor.com
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
package com.rultor.web;

import com.jcabi.manifests.Manifests;
import com.rexsl.test.XhtmlMatchers;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.XSLT;
import java.io.IOException;
import javax.xml.transform.Source;
import org.hamcrest.MatcherAssert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xembly.Directives;

/**
 * Test case for {@link DrainRs}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class DrainRsTest {

    /**
     * Pre-load test MANIFEST.MF.
     * @throws IOException If fails
     */
    @BeforeClass
    public static void manifests() throws IOException {
        Manifests.inject("Rultor-Revision", "12345");
    }

    /**
     * PostSnapshot can post-process a snapshot with ETA.
     * @throws Exception If some problem inside
     */
    @Test
    public void postProcessesSnapshot() throws Exception {
        MatcherAssert.assertThat(
            this.xml(
                new Directives()
                    .xpath("/snapshot")
                    .add("start")
                    .set("2012-08-23T13:00:00Z")
                    .up()
                    .add("eta")
                    .set("2012-08-23T14:00:00Z")
                    .up()
                    .add("updated")
                    .set("2012-08-23T13:30:00Z")
                    .up()
                    .add("steps")
                    .add("step")
                    .attr("id", "7")
                    .add("start")
                    .set("2012-08-23T13:06:00Z")
                    .up()
                    .add("finish")
                    .set("2012-08-23T13:15:00Z")
                    .toString()
            ),
            XhtmlMatchers.hasXPaths(
                "/snapshot/updated[@at='0.5']",
                "/snapshot/steps/step[@id=7]/start[@at='0.1']",
                "/snapshot/steps/step[@id=7]/finish[@at='0.25']"
            )
        );
    }

    /**
     * PostSnapshot can post-process a snapshot with ETA.
     * @throws Exception If some problem inside
     */
    @Test
    public void postProcessesSnapshotWithoutEta() throws Exception {
        MatcherAssert.assertThat(
            this.xml(
                new Directives()
                    .xpath("/snapshot")
                    .add("start")
                    .set("2012-08-23T15:00:00Z")
                    .up()
                    .add("steps")
                    .add("step")
                    .attr("id", "19")
                    .add("start")
                    .set("2012-08-23T15:06:00Z")
                    .up()
                    .add("finish")
                    .set("2012-08-23T15:15:00Z")
                    .toString()
            ),
            XhtmlMatchers.hasXPaths(
                "/snapshot/steps/step[@id=19]/start[@at='0.1']",
                "/snapshot/steps/step[@id=19]/finish[@at='0.25']"
            )
        );
    }

    /**
     * PostSnapshot can post-process a snapshot without ETA, but with UPDATED.
     * @throws Exception If some problem inside
     */
    @Test
    public void postProcessesSnapshotWithoutEtaWithUpdated() throws Exception {
        MatcherAssert.assertThat(
            this.xml(
                new Directives()
                    .xpath("/snapshot")
                    .add("version").add("revision").set("ab4ed9f").up().up()
                    .add("start").set("2012-08-23T15:00:00Z").up()
                    .add("updated").set("2012-08-23T16:00:00Z").up()
                    .add("steps")
                    .add("step").attr("id", "9")
                    .add("start")
                    .set("2012-08-23T15:30:00Z")
                    .toString()
            ),
            XhtmlMatchers.hasXPaths(
                "/snapshot/updated[@at='0.8']",
                "/snapshot/version[revision='ab4ed9f']",
                "/snapshot/steps/step[@id=9]/start[@at='0.4']"
            )
        );
    }

    /**
     * PostSnapshot can post-process a snapshot without START.
     * @throws Exception If some problem inside
     */
    @Test
    public void postProcessesSnapshotWithoutStart() throws Exception {
        MatcherAssert.assertThat(
            this.xml(
                new Directives()
                    .xpath("/snapshot")
                    .add("steps")
                    .add("step").attr("id", "3")
                    .add("start")
                    .set("2012-08-19T15:30:00Z").up().up()
                    .add("step").attr("id", "4")
                    .add("start")
                    .set("2012-08-20T15:30:00Z")
                    .toString()
            ),
            XhtmlMatchers.hasXPath(
                "/snapshot/steps/step[@id=3]/start[@at='0']"
            )
        );
    }

    /**
     * PostSnapshot can post-process an empty snapshot.
     * @throws Exception If some problem inside
     */
    @Test
    public void postProcessesEmptySnapshot() throws Exception {
        MatcherAssert.assertThat(
            this.xml("XPATH '/';"),
            XhtmlMatchers.hasXPath("/snapshot")
        );
    }

    /**
     * Make XML out of directives.
     * @param xembly Xembly
     * @return XML source
     * @throws Exception If fails
     */
    private Source xml(final String xembly) throws Exception {
        return XhtmlMatchers.xhtml(
            new XSLT(
                new Snapshot(xembly),
                this.getClass().getResourceAsStream("post.xsl")
            ).dom()
        );
    }

}
