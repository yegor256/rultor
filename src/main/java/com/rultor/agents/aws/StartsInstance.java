/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.agents.aws;

import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.ResourceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TagSpecification;
import com.jcabi.aspects.Immutable;
import com.jcabi.log.Logger;
import com.jcabi.xml.XML;
import com.rultor.agents.AbstractAgent;
import com.rultor.spi.Profile;
import java.io.IOException;
import java.util.Arrays;
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
     * Allowed instance types.
     */
    private static final String[] ALLOWED_TYPES = {
        "t2.nano", "t2.micro", "t2.small",
    };

    /**
     * Elite instance types, allowed only for special organizations.
     */
    private static final String[] ELITE_TYPES = {
        "t2.medium", "t2.large", "t2.xlarge", "t2.2xlarge",
    };

    /**
     * Elite organizations.
     */
    private static final String[] ELITE_ORGS = {
        "objectionary", "zerocracy", "yegor256",
    };

    /**
     * Profile to get EC2 params.
     */
    private final transient Profile profile;

    /**
     * AWS Client.
     */
    private final transient AwsEc2 api;

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
     * @param pfl Profile
     * @param aws API
     * @param image Instance AMI image name to run
     * @param tpe Type of instance, like "t1.micro"
     * @param grp Security group, like "sg-38924038290"
     * @param net Subnet, like "subnet-0890890"
     * @checkstyle ParameterNumberCheck (5 lines)
     */
    public StartsInstance(final Profile pfl, final AwsEc2 aws,
        final String image, final String tpe,
        final String grp, final String net) {
        super("/talk[wire/github-repo and daemon and not(ec2) and not(shell)]");
        this.profile = pfl;
        this.api = aws;
        this.image = image;
        this.type = tpe;
        this.sgroup = grp;
        this.subnet = net;
    }

    @Override
    public Iterable<Directive> process(final XML xml) throws IOException {
        final Directives dirs = new Directives();
        try {
            final Instance instance = this.run(xml.xpath("/talk/@name").get(0), xml);
            Logger.info(
                this, "EC2 instance %s started for %s",
                instance.getInstanceId(),
                xml.xpath("/talk/@name").get(0)
            );
            dirs.xpath("/talk")
                .add("ec2")
                .add("instance").set(instance.getInstanceId());
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
     * @param xml Talk XML
     * @return Instance ID
     * @throws IOException If fails
     */
    private Instance run(final String talk, final XML xml) throws IOException {
        final String itype = this.instanceType(xml);
        final RunInstancesRequest request = new RunInstancesRequest()
            .withSecurityGroupIds(this.sgroup)
            .withSubnetId(this.subnet)
            .withImageId(this.image)
            .withInstanceType(itype)
            .withMaxCount(1)
            .withMinCount(1)
            .withTagSpecifications(
                new TagSpecification()
                    .withResourceType(ResourceType.Instance)
                    .withTags(
                        new Tag().withKey("Name").withValue(talk),
                        new Tag().withKey("rultor").withValue("yes"),
                        new Tag().withKey("rultor-talk").withValue(talk)
                    )
            );
        Logger.info(
            this,
            "Starting a new AWS instance for '%s' (image=%s, type=%s, group=%s, subnet=%s)...",
            talk, this.image, itype, this.sgroup, this.subnet
        );
        final RunInstancesResult response =
            this.api.aws().runInstances(request);
        final Instance instance = response.getReservation().getInstances().get(0);
        Logger.info(
            this,
            "Started a new AWS instance %s for '%s'",
            instance.getInstanceId(), talk
        );
        return instance;
    }

    /**
     * Read one EC2 param from .rultor.xml.
     * @param xml Talk XML
     * @return Value
     * @throws IOException If fails
     */
    private String instanceType(final XML xml) throws IOException {
        final String required = new Profile.Defaults(this.profile).text(
            "/p/entry[@key='ec2']/entry[@key='type']",
            this.type
        );
        if (!Arrays.asList(StartsInstance.ALLOWED_TYPES).contains(required)
            && !Arrays.asList(StartsInstance.ELITE_TYPES).contains(required)) {
            throw new Profile.ConfigException(
                Logger.format(
                    "EC2 instance type '%s' is not valid, use one of %[list]s",
                    required, StartsInstance.ALLOWED_TYPES
                )
            );
        }
        if (Arrays.asList(StartsInstance.ELITE_TYPES).contains(required)) {
            final String org = xml.xpath("/talk/wire/github-repo/text()").get(0).split("/")[0];
            if (!Arrays.asList(StartsInstance.ELITE_ORGS).contains(org)) {
                throw new Profile.ConfigException(
                    Logger.format(
                        "You are not allowed to use EC2 instance type '%s', use one of %[list]s",
                        required, StartsInstance.ALLOWED_TYPES
                    )
                );
            }
        }
        return required;
    }
}
