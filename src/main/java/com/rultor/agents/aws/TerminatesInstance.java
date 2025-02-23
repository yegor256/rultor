/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import java.io.IOException;
import lombok.ToString;
import org.xembly.Directive;
import org.xembly.Directives;

/**
 * Stops EC2 instance, when the "daemon" is gone (the job has
 * been completed successfully).
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class TerminatesInstance extends AbstractAgent {

    /**
     * Aws Ec2 instance.
     */
    private final AwsEc2 api;

    /**
     * Ctor.
     * @param api Aws Ec2 api.
     */
    public TerminatesInstance(final AwsEc2 api) {
        super(
            "/talk/ec2[instance and host]",
            "/talk[not(daemon)]"
        );
        this.api = api;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final String instance = xml.xpath("/talk/ec2/instance/text()").get(0);
        this.api.aws().terminateInstances(
            new TerminateInstancesRequest()
                .withInstanceIds(instance)
        );
        Logger.info(
            this, "Successfully terminated %s instance of %s",
            instance, xml.xpath("/talk/@name").get(0)
        );
        return new Directives().xpath("/talk/ec2").strict(1).remove();
    }
}
