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
package com.rultor.agents.github;

import co.stateful.Counter;
import co.stateful.Counters;
import com.jcabi.aspects.Immutable;
import com.jcabi.github.Comment;
import com.jcabi.github.Github;
import com.jcabi.github.Issue;
import com.jcabi.log.Logger;
import com.rultor.spi.SuperAgent;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Hex;
import org.xembly.Directives;

/**
 * Starts talk when I'm mentioned in a Github issue.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "github", "counters" })
public final class StartsTalks implements SuperAgent {

    /**
     * Github.
     */
    private final transient Github github;

    /**
     * Github.
     */
    private final transient Counters counters;

    /**
     * Ctor.
     * @param ghub Github client
     * @param ctrs Counters
     */
    public StartsTalks(final Github ghub, final Counters ctrs) {
        this.github = ghub;
        this.counters = ctrs;
    }

    @Override
    public void execute(final Talks talks) throws IOException {
        final Counter threshold = this.counters.get("rt-threshold");
        final Iterable<Issue> issues = this.github.search().issues(
            String.format(
                "mentions:%s updated:>=%tF",
                this.github.search().github().users().self().login(),
                new Date(threshold.incrementAndGet(0L))
            ),
            "updated",
            "asc"
        );
        final Counter cnt = this.counters.get("rt-latest");
        final int latest = (int) cnt.incrementAndGet(0L);
        for (final Issue issue : issues) {
            final int last = StartsTalks.last(issue);
            if (last <= latest) {
                continue;
            }
            final String coords = issue.repo().coordinates().toString();
            final String name = Hex.encodeHexString(
                String.format("%s#%d", coords, issue.number()).getBytes()
            );
            if (!talks.exists(name)) {
                talks.create(name);
            }
            final Talk talk = talks.get(name);
            talk.active(true);
            if (talk.read().nodes("/talk/wire").isEmpty()) {
                talks.get(name).modify(
                    new Directives().xpath("/talk")
                        .add("wire")
                        .add("github-repo").set(coords).up()
                        .add("github-issue")
                        .set(Integer.toString(issue.number()))
                );
            }
            Logger.info(
                this, "talk %s#%d activated with message #%d",
                coords, issue.number(), last
            );
        }
        cnt.set((long) latest);
        threshold.set(System.currentTimeMillis());
    }

    /**
     * Latest message number in the issue.
     * @param issue The issue
     * @return Message number
     */
    private static int last(final Issue issue) {
        int last = 0;
        for (final Comment comment : issue.comments().iterate()) {
            if (comment.number() > last) {
                last = comment.number();
            }
        }
        return last;
    }

}
