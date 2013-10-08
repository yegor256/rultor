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
package com.rultor.guard;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.log.Logger;
import com.rultor.shell.Batch;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.TagLine;
import com.rultor.spi.Instance;
import com.rultor.stateful.ConcurrentNotepad;
import java.io.IOException;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.output.NullOutputStream;

/**
 * On pull request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "requests", "busy", "batch" })
@Loggable(Loggable.DEBUG)
public final class OnPullRequest implements Instance {

    /**
     * All available pull requests.
     */
    private final transient MergeRequests requests;

    /**
     * List of requests we're busy with at the moment.
     */
    private final transient ConcurrentNotepad busy;

    /**
     * Batch to execute.
     */
    private final transient Batch batch;

    /**
     * Public ctor.
     * @param rqsts Requests
     * @param ntp Notepad
     * @param btch Batch to use
     */
    public OnPullRequest(
        @NotNull(message = "requests can't be NULL") final MergeRequests rqsts,
        @NotNull(message = "notepad can't be NULL") final ConcurrentNotepad ntp,
        @NotNull(message = "batch can't be NULL") final Batch btch) {
        this.requests = rqsts;
        this.busy = ntp;
        this.batch = btch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        final Iterator<MergeRequest> iterator = this.requests.iterator();
        while (iterator.hasNext()) {
            final MergeRequest request = iterator.next();
            if (!this.busy.addIf(request.name())) {
                continue;
            }
            try {
                this.merge(request);
            } finally {
                this.busy.remove(request.name());
            }
        }
    }

    /**
     * Merge this pull request.
     * @param request The request to merge
     * @return TRUE if success
     * @throws IOException If IO problem
     */
    @Step(
        before = "merging request ${args[0].name}",
        // @checkstyle LineLength (1 line)
        value = "merge request ${args[0].name} #if($result)succeeded#{else}failed#end"
    )
    private boolean merge(final MergeRequest request) throws IOException {
        request.started();
        final long start = System.currentTimeMillis();
        final int code = this.batch.exec(
            new ArrayMap<String, String>()
                .with("request", request.name())
                // @checkstyle MultipleStringLiterals (4 lines)
                .with("srcSCM", request.source().scm().uri().toString())
                .with("srcBranch", request.source().name())
                .with("destSCM", request.destination().scm().uri().toString())
                .with("destBranch", request.destination().name()),
            new NullOutputStream()
        );
        final boolean success = code == 0;
        final long millis = System.currentTimeMillis() - start;
        new TagLine("on-pull-request")
            .fine(success)
            .attr("code", Integer.toString(code))
            .attr("duration", Long.toString(millis))
            .attr("name", request.name())
            .attr("srcSCM", request.source().scm().uri().toString())
            .attr("srcBranch", request.source().name())
            .attr("destSCM", request.destination().scm().uri().toString())
            .attr("destBranch", request.destination().name())
            .markdown(
                Logger.format(
                    "merge request %s from `%s` to `%s` %s in %[ms]s",
                    request.name(), request.source().name(),
                    request.destination().name(),
                    // @checkstyle AvoidInlineConditionals (1 line)
                    success ? "succeeded" : "failed",
                    millis
                )
            )
            .log();
        if (success) {
            request.accept();
        } else {
            request.reject();
        }
        return success;
    }

}
