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
package com.rultor.cd;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.rultor.ci.Build;
import com.rultor.shell.Batch;
import com.rultor.snapshot.Snapshot;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Instance;
import com.rultor.tools.Exceptions;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directives;
import org.xembly.ImpossibleModificationException;

/**
 * On deployment request.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "deployments", "batch" })
@Loggable(Loggable.DEBUG)
public final class OnDeploy implements Instance {

    /**
     * All available deployment requests.
     */
    private final transient Array<Deployment> deployments;

    /**
     * Batch to execute.
     */
    private final transient Batch batch;

    /**
     * Public ctor.
     * @param deps Deployment requests
     * @param btch Batch to use
     */
    public OnDeploy(
        @NotNull(message = "deployments can't be NULL")
        final Collection<Deployment> deps,
        @NotNull(message = "batch can't be NULL") final Batch btch) {
        this.deployments = new Array<Deployment>(deps);
        this.batch = btch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        for (Deployment dep : this.deployments) {
            this.deploy(dep);
        }
    }

    /**
     * Deploy it.
     * @param dep Deployment
     * @return TRUE if success
     * @throws IOException If IO problem
     */
    @Step(
        before = "deploying request ${args[0].name}",
        // @checkstyle LineLength (1 line)
        value = "deployment ${args[0].name} #if($result)successfull#{else}failed#end"
    )
    private boolean deploy(final Deployment dep) throws IOException {
        final String tag = "on-deploy";
        final Snapshot snapshot = new Build(tag, this.batch).exec(
            new ImmutableMap.Builder<String, Object>()
                .putAll(dep.params())
                .build()
        );
        final boolean failure = this.failure(snapshot, tag);
        if (failure) {
            dep.failed(snapshot);
        } else {
            dep.succeeded(snapshot);
        }
        this.tag(dep, failure);
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

    /**
     * Log a tag.
     * @param dep Deployment request
     * @param failure TRUE if failed
     * @throws IOException If fails
     */
    private void tag(final Deployment dep, final boolean failure)
        throws IOException {
        final StringWriter data = new StringWriter();
        final JsonGenerator json = Json.createGenerator(data)
            .writeStartObject()
            .write("request", dep.name())
            .writeStartObject("params");
        for (Map.Entry<String, Object> entry : dep.params().entrySet()) {
            json.write(entry.getKey(), entry.getValue().toString());
        }
        json.writeEnd()
            .write("failure", Boolean.toString(failure))
            .writeEnd()
            .close();
        final StringBuilder desc = new StringBuilder();
        desc.append("deployment request ").append(dep.name());
        if (failure) {
            desc.append(" failed");
        } else {
            desc.append(" succeeded");
        }
        new XemblyLine(
            new Directives()
                .xpath("/snapshot").strict(1).addIfAbsent("tags")
                .add("tag").add("label").set("deploy").up()
                .add("level").set(Level.INFO.toString()).up()
                .add("data").set(data.toString()).up()
                .add("markdown").set(desc.toString())
        ).log();
    }

}
