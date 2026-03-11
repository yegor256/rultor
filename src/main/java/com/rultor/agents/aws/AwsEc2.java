/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.jcabi.aspects.Immutable;
import lombok.ToString;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;

/**
 * Amazon EC2 client.
 *
 * @since 1.77
 */
@Immutable
@ToString
public final class AwsEc2 {
    /**
     * Access key.
     */
    private final transient String key;

    /**
     * Secret key.
     */
    private final transient String secret;

    /**
     * AWS region.
     */
    private final transient String region;

    /**
     * Ctor.
     * @param akey Key to use api
     * @param asecret Secret to use api
     */
    public AwsEc2(final String akey, final String asecret) {
        this(akey, asecret, "us-east-1");
    }

    /**
     * Ctor.
     * @param akey Key to use api
     * @param asecret Secret to use api
     * @param reg Region for instance run
     */
    public AwsEc2(final String akey, final String asecret, final String reg) {
        this.key = akey;
        this.secret = asecret;
        this.region = reg;
    }

    /**
     * AWS EC2 client instance.
     * @return AWS EC2 client
     */
    public Ec2Client aws() {
        return Ec2Client.builder()
            .region(Region.of(this.region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(this.key, this.secret)
                )
            )
            .build();
    }
}
