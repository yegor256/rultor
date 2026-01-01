/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.jcabi.aspects.Immutable;
import lombok.ToString;

/**
 * Amazon EC2 client.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class AwsEc2 {
    /**
     * Builder to get client.
     */
    private final transient AmazonEC2ClientBuilder client;

    /**
     * Ctor.
     * @param key Key to ise api
     * @param secret Secret to use api
     */
    public AwsEc2(final String key, final String secret) {
        this(key, secret, "us-east-1");
    }

    /**
     * Ctor.
     * @param key Key to use api
     * @param secret Secret to use api
     * @param region Region for instance run
     */
    public AwsEc2(final String key, final String secret, final String region) {
        this.client = AmazonEC2ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(key, secret)
                )
            );
    }

    /**
     * AWS EC2 client instance.
     * @return AWS EC2 client
     */
    public AmazonEC2 aws() {
        return this.client.build();
    }
}
