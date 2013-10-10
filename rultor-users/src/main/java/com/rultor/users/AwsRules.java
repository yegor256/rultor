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
package com.rultor.users;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Rule;
import com.rultor.spi.Rules;
import com.rultor.spi.Spec;
import com.rultor.spi.Wallet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Rules in DynamoDB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "region", "client", "owner" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
final class AwsRules implements Rules {

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * SQS client.
     */
    private final transient SQSClient client;

    /**
     * URN of the user.
     */
    private final transient URN owner;

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     * @param sqs SQS client
     * @param urn URN of the user
     */
    protected AwsRules(final Region reg, final SQSClient sqs, final URN urn) {
        this.region = reg;
        this.client = sqs;
        this.owner = urn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull(message = "list of rules of a user is never NULL")
    public Iterator<Rule> iterator() {
        final Iterator<Item> items = this.fetch().iterator();
        return new Iterator<Rule>() {
            @Override
            public boolean hasNext() {
                return items.hasNext();
            }
            @Override
            public Rule next() {
                return AwsRules.this.toRule(items.next());
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushAfter
    @RetryOnFailure(verbose = false)
    public void create(
        @NotNull(message = "rule name is mandatory when creating new rule")
        @Pattern(
            regexp = "[a-z][-a-z0-9]{2,}",
            message = "Only numbers, letters, and dashes are allowed"
        )
        final String unt) {
        if (this.contains(unt)) {
            throw new IllegalArgumentException(
                String.format("Rule `%s` already exists", unt)
            );
        }
        this.region.table(AwsRule.TABLE).put(
            new Attributes()
                .with(AwsRule.HASH_OWNER, this.owner.toString())
                .with(AwsRule.RANGE_NAME, unt)
                .with(AwsRule.FIELD_SPEC, new Spec.Simple().asText())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushAfter
    @RetryOnFailure(verbose = false)
    public void remove(@NotNull(message = "rule name is mandatory")
        final String rule) {
        final Iterator<Item> items = this.region.table(AwsRule.TABLE).frame()
            .where(AwsRule.HASH_OWNER, this.owner.toString())
            .where(AwsRule.RANGE_NAME, rule)
            .through(new QueryValve())
            .iterator();
        if (!items.hasNext()) {
            throw new NoSuchElementException(
                String.format("Rule `%s` not found", rule)
            );
        }
        items.next();
        items.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    @RetryOnFailure(verbose = false)
    public Rule get(
        @NotNull(message = "rule name can't be NULL")
        @Pattern(regexp = ".+", message = "rule shouldn't be blank")
        final String rule) {
        final Collection<Item> items = this.region.table(AwsRule.TABLE)
            .frame()
            .where(AwsRule.HASH_OWNER, this.owner.toString())
            .where(AwsRule.RANGE_NAME, rule)
            .through(new QueryValve());
        if (items.isEmpty()) {
            throw new NoSuchElementException(
                String.format("Rule `%s` doesn't exist", rule)
            );
        }
        return this.toRule(items.iterator().next());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    @RetryOnFailure(verbose = false)
    public boolean contains(
        @NotNull(message = "rule name can't be NULL")
        @Pattern(regexp = ".+", message = "rule shouldn't be blank")
        final String rule) {
        return !this.region.table(AwsRule.TABLE)
            .frame()
            .where(AwsRule.HASH_OWNER, this.owner.toString())
            .where(AwsRule.RANGE_NAME, rule)
            .through(new QueryValve())
            .isEmpty();
    }

    /**
     * Fetch them all.
     * @return All Rules
     */
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    @RetryOnFailure(verbose = false)
    private Collection<Item> fetch() {
        return this.region.table(AwsRule.TABLE)
            .frame()
            .where(AwsRule.HASH_OWNER, this.owner.toString())
            .through(
                new QueryValve().withAttributesToGet(
                    AwsRule.FIELD_SPEC,
                    AwsRule.FIELD_DRAIN,
                    AwsRule.FIELD_FAILURE
                )
            );
    }

    /**
     * Marker to flush cash.
     */
    @Cacheable.FlushAfter
    private void flush() {
        // nothing todo
    }

    /**
     * Convert item to rule.
     * @param item Item from AWS
     * @return Rule
     */
    private Rule toRule(final Item item) {
        return new CacheableRule(new AwsRule(this.client, item));
    }

    /**
     * Rule that caches its calls.
     */
    @ToString
    @EqualsAndHashCode(of = "origin")
    @Loggable(Loggable.DEBUG)
    private final class CacheableRule implements Rule {
        /**
         * Original rule.
         */
        private final transient Rule origin;
        /**
         * Ctor.
         * @param rule Rule to wrap
         */
        protected CacheableRule(final Rule rule) {
            this.origin = rule;
        }
        @Override
        @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
        public String name() {
            return this.origin.name();
        }
        @Override
        @Cacheable.FlushAfter
        public void update(final Spec spec, final Spec drain) {
            this.origin.update(spec, drain);
            AwsRules.this.flush();
        }
        @Override
        @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
        public Spec spec() {
            return this.origin.spec();
        }
        @Override
        @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
        public Spec drain() {
            return this.origin.drain();
        }
        @Override
        @Cacheable.FlushAfter
        public void failure(final String desc) {
            this.origin.failure(desc);
            AwsRules.this.flush();
        }
        @Override
        @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
        public String failure() {
            return this.origin.failure();
        }
        // @checkstyle RedundantThrows (5 lines)
        @Override
        public Wallet wallet(final Coordinates work, final URN taker,
            final String rule) throws Wallet.NotEnoughFundsException {
            return this.origin.wallet(work, taker, rule);
        }
    }

}
