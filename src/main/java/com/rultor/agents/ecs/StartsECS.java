/**
 * Copyright (c) 2009-2017, rultor.com
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
package com.rultor.agents.ecs;

import com.amazonaws.services.ecs.model.Container;
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
 * Starts Amazon ECS container, that runs Docker + SSHD on ECS. Container
 * is configured in privileged mode. Configuration is stored in S3.
 * See details here:
 * @link http://docs.aws.amazon.com/AmazonECS/latest/developerguide/ecs-agent-config.html#ecs-config-s3
 * @author Yuriy Alevohin (alevohin@mail.ru)
 * @version $Id$
 * @since 2.0
 * @todo #629 Implement com.rultor.agents.ecs.StopsECS agent. It must
 *  stop ECS on-demand container if it was started at StartsECS agent.
 *  StopsECS must use container ARN from /talk/ec2/[@id] to stop it.
 * @todo #629 Add new container creation classes for StartsECS and StopsECS
 *  to com.rultor.agents.Agents. StartsECS must be invoked before
 *  RegistersShell agent. StopsECS must be invoked after RemovesShell agent.
 * @todo #629 Implement the ability to use Rultor with any AWS account,
 *  by providing the credentials to it via the encrypted assets and a
 *  respective configuration setting in the .rultor.yml. Document the
 *  implementation properly in the Rultor documentation.
 */
@Immutable
@ToString
@EqualsAndHashCode(callSuper = false, of = { "amazon" })
public final class StartsECS extends AbstractAgent {
    /**
     * AmazonECS client provider.
     */
    private final transient Amazon amazon;

    /**
     * Ctor.
     * @param amaz Amazon
     */
    public StartsECS(final Amazon amaz) {
        super("/talk[daemon and not(shell)]");
        this.amazon = amaz;
    }

    @Override
    // @todo #629 Add Container params to Directive, for example publicIpAddress
    //  Let's fix this when possible. The ticket will be in GitHub.
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Container container = this.amazon.runOnDemand();
        Logger.info(
            this,
            "ECS container %s created",
            container
        );
        return new Directives().xpath("/talk")
            .add("ec2")
            .attr("id", container.getContainerArn());
    }
}
