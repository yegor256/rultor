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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.log.Logger;
import com.rultor.shell.Batch;
import com.rultor.snapshot.Step;
import com.rultor.snapshot.TagLine;
import com.rultor.spi.Instance;
import java.io.IOException;
import java.util.Collection;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.output.NullOutputStream;

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

    @Override
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void pulse() throws Exception {
        for (final Deployment dep : this.deployments) {
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
        before = "deploying ${args[0].name}",
        // @checkstyle LineLength (1 line)
        value = "deployment ${args[0].name} #if($result)succeeded#{else}failed#end"
    )
    private boolean deploy(final Deployment dep) throws IOException {
        final long start = System.currentTimeMillis();
        final int code = this.batch.exec(
            new ArrayMap<String, String>().with("deployment", dep.name()),
            new NullOutputStream()
        );
        final long millis = System.currentTimeMillis() - start;
        new TagLine("on-deploy")
            .fine(code == 0)
            .attr("code", Integer.toString(code))
            .attr("duration", Long.toString(millis))
            .attr("name", dep.name())
            .markdown(
                Logger.format(
                    "deployment `%s` %s in %[ms]s",
                    dep.name(),
                    // @checkstyle AvoidInlineConditionals (1 line)
                    code == 0 ? "succeeded" : "failed",
                    millis
                )
            )
            .log();
        if (code == 0) {
            dep.succeeded();
        } else {
            dep.failed();
        }
        return code == 0;
    }

}
