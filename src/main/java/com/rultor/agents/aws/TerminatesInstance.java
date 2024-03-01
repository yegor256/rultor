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
