/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;
import software.amazon.awssdk.services.ec2.model.DescribeInstanceStatusRequest;

/**
 * Removes "EC2" element if instance is already "terminated".
 * @since 1.77
 */
@Immutable
@ToString
public final class DetachesInstance extends AbstractAgent {

    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * Ctor.
     * @param aws API
     */
    public DetachesInstance(final AwsEc2 aws) {
        super("/talk/ec2/instance");
        this.api = aws;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String instance = xml.xpath("/talk/ec2/instance/text()").get(0);
        final Directives dirs = new Directives();
        if ("terminated".equals(
            this.api.aws().describeInstanceStatus(
                DescribeInstanceStatusRequest.builder()
                    .includeAllInstances(true)
                    .instanceIds(instance)
                    .build()
            ).instanceStatuses().get(0).instanceState().nameAsString()
        )) {
            dirs.xpath("/talk/ec2").strict(1).remove();
            Logger.info(this, "AWS instance %s is already terminated, detaching", instance);
        }
        return dirs;
    }
}
