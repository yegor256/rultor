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
package com.rultor.aws;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * EC2 client.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface EC2Client {

    /**
     * Get AWS EC2 client.
     * @return Get it
     */
    AmazonEC2 get();

    /**
     * Simple client.
     */
    @Immutable
    @EqualsAndHashCode(of = { "key", "secret" })
    @Loggable(Loggable.DEBUG)
    final class Simple implements EC2Client {
        /**
         * Key.
         */
        private final transient String key;
        /**
         * Secret.
         */
        private final transient String secret;
        /**
         * Public ctor.
         * @param akey AWS key
         * @param scrt AWS secret
         */
        public Simple(
            @NotNull(message = "AWS key can't be NULL") final String akey,
            @NotNull(message = "AWS secret can't be NULL") final String scrt) {
            this.key = akey;
            this.secret = scrt;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("`%s`", this.key);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public AmazonEC2 get() {
            return new AmazonEC2Client(
                new BasicAWSCredentials(this.key, this.secret)
            );
        }
    }

    /**
     * With custom region.
     */
    @Immutable
    @EqualsAndHashCode(of = { "region", "origin" })
    @Loggable(Loggable.DEBUG)
    final class Regional implements EC2Client {
        /**
         * Original client.
         */
        private final transient EC2Client origin;
        /**
         * Region to use.
         */
        private final transient String region;
        /**
         * Public ctor.
         * @param reg Region we're in
         * @param client Original client
         */
        public Regional(
            @NotNull(message = "region can't be NULL") final String reg,
            @NotNull(message = "client can't be NULL") final EC2Client client) {
            Validate.isTrue(
                reg.matches("[a-z]{2}\\-[a-z]+\\-\\d+"),
                "AWS region `%s` is in wrong format", reg
            );
            this.region = reg;
            this.origin = client;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("%s in %s", this.origin, this.region);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public AmazonEC2 get() {
            final AmazonEC2 aws = this.origin.get();
            aws.setRegion(RegionUtils.getRegion(this.region));
            return aws;
        }
    }

}
