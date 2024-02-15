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

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.Tag;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import lombok.ToString;

/**
 * Amazon EC2 instance.
 *
 * @since 1.77
 */
@Immutable
@ToString
@SuppressWarnings({"PMD.ShortMethodName",
    "PMD.ConstructorOnlyInitializesOrCallOtherConstructors",
    "PMD.AvoidFieldNameMatchingMethodName"
})
public final class AwsEc2Instance {
    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

    /**
     * AWS Instance id.
     */
    private final String id;

    /**
     * Ctor.
     * @param api AwsEc2 api client
     * @param id Instance id
     */
    public AwsEc2Instance(final AwsEc2 api, final String id) {
        if (id.isEmpty()) {
            throw new IllegalArgumentException(
                "Instance id is mandatory"
            );
        }
        this.api = api;
        this.id = id;
    }

    /**
     * Stop instance.
     */
    public void stop() {
        final DryRunSupportedRequest<StopInstancesRequest> draft = () -> {
            final StopInstancesRequest request = new StopInstancesRequest()
                .withInstanceIds(this.id);
            return request.getDryRunRequest();
        };
        final AmazonEC2 client = this.api.aws();
        final DryRunResult<StopInstancesRequest> response =
            client.dryRun(draft);
        if (!response.isSuccessful()) {
            Logger.error(
                this,
                "Failed dry run to stop instance %s", this.id
            );
            throw response.getDryRunResponse();
        }
        final StopInstancesRequest request = new StopInstancesRequest()
            .withInstanceIds(this.id);
        client.stopInstances(request);
        Logger.info("Successfully stop instance %s", this.id);
    }

    /**
     * Add a tag for instance.
     * @param key Tag name
     * @param tag Tag value
     * @return This instance
     */
    public AwsEc2Instance tag(final String key, final String tag) {
        final Tag awstag = new Tag()
            .withKey(key)
            .withValue(tag);
        final CreateTagsRequest request = new CreateTagsRequest()
            .withResources(this.id)
            .withTags(awstag);
        this.api.aws().createTags(request);
        return this;
    }

    /**
     * Instance id.
     * @return Instance Id
     * @checkstyle MethodNameCheck (3 lines)
     */
    public String id() {
        return this.id;
    }
}
