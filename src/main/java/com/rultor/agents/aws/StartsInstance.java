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

import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.agents.shells.PfShell;
import com.rultor.spi.Profile;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Starts EC2 instance.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class StartsInstance extends AbstractAgent {

    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * Shell.
     */
    private final transient PfShell shell;

    /**
     * Amazon machine image id.
     */
    private final String image;

    /**
     * AWS Instance type.
     */
    private final transient String type;

    /**
     * EC2 security group.
     */
    private final String sgroup;

    /**
     * EC2 subnet.
     */
    private final String subnet;

    /**
     * Ctor.
     * @param aws API
     * @param shll The shell
     * @param image Instance AMI image name to run
     * @param tpe Type of instance, like "t1.micro"
     * @param grp Security group, like "sg-38924038290"
     * @param net Subnet, like "subnet-0890890"
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public StartsInstance(final AwsEc2 aws,
        final PfShell shll, final String image, final String tpe,
        final String grp, final String net) {
        super("/talk[daemon and not(shell)]");
        this.api = aws;
        this.shell = shll;
        this.image = image;
        this.type = tpe;
        this.sgroup = grp;
        this.subnet = net;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String hash = xml.xpath("/talk/daemon/@id").get(0);
        final Directives dirs = new Directives();
        try {
            final String login = this.shell.login();
            if (login.isEmpty()) {
                throw new Profile.ConfigException(
                    "SSH login is empty, it's a mistake"
                );
            }
            final String key = this.shell.key();
            if (key.isEmpty()) {
                throw new Profile.ConfigException(
                    "SSH key is empty, it's a mistake"
                );
            }
            final Instance instance = this.run(xml.xpath("/talk/@name").get(0));
            Logger.info(
                this, "EC2 instance %s on %s started in %s",
                instance.getInstanceId(), instance.getPublicIpAddress(),
                xml.xpath("/talk/@name").get(0)
            );
            dirs.xpath("/talk").add("ec2")
                .attr("id", instance.getInstanceId());
            dirs.xpath("/talk").add("shell")
                .attr("id", hash)
                .add("host").set(instance.getPublicDnsName()).up()
                .add("port").set(Integer.toString(this.shell.port())).up()
                .add("login").set(login).up()
                .add("key").set(key);
        } catch (final Profile.ConfigException ex) {
            dirs.xpath("/talk/daemon/script").set(
                String.format(
                    "Failed to read profile: %s", ex.getLocalizedMessage()
                )
            );
        }
        return dirs;
    }

    /**
     * Run a new instance.
     * @param talk Name of the talk
     * @return Instance ID
     */
    private Instance run(final String talk) {
        final RunInstancesRequest request = new RunInstancesRequest()
            .withSecurityGroupIds(this.sgroup)
            .withSubnetId(this.subnet)
            .withImageId(this.image)
            .withInstanceType(this.type)
            .withMaxCount(1)
            .withMinCount(1);
        Logger.info(
            this,
            "Starting a new AWS instance, image=%s, type=%s, group=%s, subnet=%s ...",
            this.image, this.type, this.sgroup, this.subnet
        );
        final RunInstancesResult response =
            this.api.aws().runInstances(request);
        final Instance instance = response.getReservation().getInstances().get(0);
        final String iid = instance.getInstanceId();
        this.api.aws().createTags(
            new CreateTagsRequest()
                .withResources(iid)
                .withTags(new Tag().withKey("name").withValue(talk))
        );
        while (true) {
            final DescribeInstanceStatusResult res = this.api.aws().describeInstanceStatus(
                new DescribeInstanceStatusRequest()
                    .withIncludeAllInstances(true)
                    .withInstanceIds(iid)
            );
            final InstanceState state = res.getInstanceStatuses().get(0).getInstanceState();
            Logger.info(this, "AWS instance %s state: %s", state.getName());
            if ("running".equals(state.getName())) {
                break;
            }
        }
        Logger.info(this, "AWS instance %s launched and running", iid);
        return instance;
    }
}
