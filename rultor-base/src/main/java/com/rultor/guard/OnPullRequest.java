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

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.rultor.ci.Build;
import com.rultor.shell.Batch;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.Tag;
import com.rultor.spi.Instance;
import com.rultor.stateful.ConcurrentNotepad;
import com.rultor.tools.Exceptions;
import java.io.IOException;
import java.util.Iterator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.xembly.ImpossibleModificationException;

/**
 * On pull request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
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
            if (!this.busy.addIfAbsent(request.name())) {
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
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Logger.format(
            "on pull request from %s executes %s and track it in %s",
            this.requests,
            this.batch,
            this.busy
        );
    }

    /**
     * Merge this pull request.
     * @param request The request to merge
     * @return TRUE if success
     * @throws IOException If IO problem
     */
    @Tag("merge")
    @Step(
        before = "building merge request ${args[0].name()}",
        // @checkstyle LineLength (1 line)
        value = "merge request ${args[0].name()} #if($result)built successfully#{else}failed to build#end"
    )
    private boolean merge(final MergeRequest request) throws IOException {
        final String tag = "on-pull-request";
        request.started();
        final Snapshot snapshot = new Build(tag, this.batch).exec(
            new ImmutableMap.Builder<String, Object>()
                .putAll(request.params())
                .build()
        );
        final boolean failure = this.failure(snapshot, tag);
        if (failure) {
            request.reject(snapshot);
        } else {
            request.accept(snapshot);
        }
        return !failure;
    }

    /**
     * Was it a failed merge?
     * @param snapshot Snapshot received
     * @param tag Tag to look for
     * @return TRUE if it was a failure
     */
    private boolean failure(final Snapshot snapshot, final String tag) {
        boolean failure = true;
        try {
            failure = snapshot.xml()
                .nodes(String.format("//tag[label='%s' and level='FINE']", tag))
                .isEmpty();
        } catch (ImpossibleModificationException ex) {
            Exceptions.warn(this, ex);
        }
        return failure;
    }

}
