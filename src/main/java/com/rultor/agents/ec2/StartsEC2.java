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
package com.rultor.agents.ec2;

import com.amazonaws.services.ec2.model.Instance;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Starts Amazon EC2 instance.
 * @author Yuriy Alevohin (alevohin@mail.ru)
 * @version $Id$
 * @since 2.0
 * @todo #629 Implement com.rultor.agents.ec2.StopsEC2 agent. It must
 *  stopped EC2 on-demand instance if it was started at StartsEC2 agent.
 *  StopsEC2 must use instance id from /talk/ec2/[@id] to stop it.
 * @todo #629 RegistersShell must register SSH params "host", "port",
 *  "login", "key" for ec2 on-demand instance, if this one was successfully
 *  started. Successfully start means that these parameters exist in
 *  /talk/ec2
 * @todo #629 Add new instance creation classes for StartsEC2 and StopsEC2
 *  to com.rultor.agents.Agents. StartsEC2 must be invoked before
 *  RegistersShell agent. StopsEC2 must be invoked after RemovesShell agent.
 * @todo #629 Write documentation for configuring ec2 via .rultor.yml at
 *  2014-07-13-reference.md
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "amazon" })
public final class StartsEC2 extends AbstractAgent {
    /**
     * AmazonEC2 client provider.
     */
    private final transient Amazon amazon;

    /**
     * Ctor.
     * @param amaz Amazon
     */
    public StartsEC2(final Amazon amaz) {
        super("/talk[daemon and not(shell)]");
        this.amazon = amaz;
    }

    @Override
    //@todo #629 Add Instance params to Directive, for example publicIpAddress
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Instance instance = this.amazon.runOnDemand();
        Logger.info(
            this,
            "EC2 instance %s created",
            instance
        );
        return new Directives().xpath("/talk")
            .add("ec2")
            .attr("id", instance.getInstanceId());
    }
}
