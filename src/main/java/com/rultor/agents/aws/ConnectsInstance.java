/*
 * Copyright (c) 2009-2024 Yegor Bugayenko
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
package com.rultor.agents.aws;

import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.PfShell;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Connects a running EC2 instance: detects its IP.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class ConnectsInstance extends AbstractAgent {

    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * Shell.
     */
    private final transient PfShell shell;

    /**
     * Ctor.
     * @param aws API
     * @param shll The shell
     */
    public ConnectsInstance(final AwsEc2 aws, final PfShell shll) {
        super("/talk[daemon and ec2 and not(shell)]");
        this.api = aws;
        this.shell = shll;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String instance = xml.xpath("/talk/ec2/@id").get(0);
        while (true) {
            final DescribeInstanceStatusResult res = this.api.aws().describeInstanceStatus(
                new DescribeInstanceStatusRequest()
                    .withIncludeAllInstances(true)
                    .withInstanceIds(instance)
            );
            final InstanceState state = res.getInstanceStatuses().get(0).getInstanceState();
            Logger.info(this, "AWS instance %s state: %s", instance, state.getName());
            if ("running".equals(state.getName())) {
                break;
            }
            new Sleep(5L).now();
        }
        final Instance ready = this.api.aws().describeInstances(
            new DescribeInstancesRequest()
                .withInstanceIds(instance)
        ).getReservations().get(0).getInstances().get(0);
        Logger.info(
            this, "AWS instance %s launched and running at %s",
            ready.getInstanceId(), ready.getPublicIpAddress()
        );
        new Sleep(60L).now();
        final Directives dirs = new Directives();
        dirs.xpath("/talk").add("shell")
            .attr("id", xml.xpath("/talk/daemon/@id").get(0))
            .add("host").set(ready.getPublicDnsName()).up()
            .add("port").set(Integer.toString(this.shell.port())).up()
            .add("login").set(this.shell.login()).up()
            .add("key").set(this.shell.key());
        return dirs;
    }
}
