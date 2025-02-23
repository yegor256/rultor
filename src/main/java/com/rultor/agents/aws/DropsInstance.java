/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Deletes the "ec2" XML element if the instance doesn't exist in EC2 --
 * this means that the instance was killed by some other mechanisms
 * and doesn't need to be connected anymore to the talk.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class DropsInstance extends AbstractAgent {

    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * Ctor.
     * @param aws API
     */
    public DropsInstance(final AwsEc2 aws) {
        super("/talk/ec2/instance");
        this.api = aws;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String instance = xml.xpath("/talk/ec2/instance/text()").get(0);
        final DescribeInstancesResult res = this.api.aws().describeInstances(
            new DescribeInstancesRequest()
                .withInstanceIds(instance)
        );
        final Directives dirs = new Directives();
        if (res.getReservations().isEmpty()) {
            dirs.xpath("/talk/ec2").strict(1).remove();
            Logger.warn(
                this, "AWS instance %s is absent, deleting the link from %s",
                instance, xml.xpath("/talk/@name").get(0)
            );
        }
        return dirs;
    }

}
