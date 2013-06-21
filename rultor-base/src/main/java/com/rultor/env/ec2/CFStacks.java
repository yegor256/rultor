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
package com.rultor.env.ec2;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.rultor.env.Environment;
import com.rultor.env.Environments;
import java.io.IOException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * CloudFormation Stacks.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "template", "client" })
@Loggable(Loggable.DEBUG)
public final class CFStacks implements Environments {

    /**
     * Template.
     */
    private final transient String template;

    /**
     * CF client.
     */
    private final transient CFClient client;

    /**
     * Public ctor.
     * @param tmpl Template
     * @param akey AWS key
     * @param scrt AWS secret
     */
    public CFStacks(final String tmpl, final String akey, final String scrt) {
        this(tmpl, new CFClient.Simple(akey, scrt));
    }

    /**
     * Public ctor.
     * @param tmpl Template
     * @param clnt CF client
     */
    public CFStacks(final String tmpl, final CFClient clnt) {
        this.template = tmpl;
        this.client = clnt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Environment acquire() throws IOException {
        final AmazonCloudFormation aws = this.client.get();
        try {
            final CreateStackResult result = aws.createStack(
                new CreateStackRequest()
                    .withTemplateBody(this.template)
            );
            return new CFStack(result.getStackId(), this.client);
        } finally {
            aws.shutdown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String face() {
        return "EC2 instance through CloudFormation";
    }

}
