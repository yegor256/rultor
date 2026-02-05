/*
 * SPDX-FileCopyrightText: Copyright (c) 2009-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.rultor.dynamo;

import co.stateful.Counter;
import com.google.common.collect.Iterables;
import com.jcabi.aspects.Immutable;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.util.Date;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.HeadOf;
import org.cactoos.iterable.Mapped;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.Select;

/**
 * Talks in Dynamo.
 *
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "region")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.DataClass"})
public final class DyTalks implements Talks {

    /**
     * Table name.
     */
    public static final String TBL = "talks";

    /**
     * Index name.
     * @checkstyle MultipleStringLiteralsCheck (3 lines)
     */
    public static final String IDX_ACTIVE = "active";

    /**
     * Index name.
     * @since 1.3
     */
    public static final String IDX_NUMBERS = "numbers";

    /**
     * Index name.
     * @since 1.23
     */
    public static final String IDX_SIBLINGS = "siblings";

    /**
     * Talk unique name.
     */
    public static final String HASH = "name";

    /**
     * Its number.
     * @since 1.3
     */
    public static final String ATTR_NUMBER = "number";

    /**
     * Name of the repo it belongs to.
     * @since 1.23
     */
    public static final String ATTR_REPO = "repo";

    /**
     * Is it active (1) or archived (0).
     */
    public static final String ATTR_ACTIVE = "active";

    /**
     * XML of the talk.
     */
    public static final String ATTR_XML = "xml";

    /**
     * XML of the talk, gzip.
     */
    public static final String ATTR_XML_ZIP = "zipxml";

    /**
     * When updated.
     */
    public static final String ATTR_UPDATED = "updated";

    /**
     * Region we're in.
     */
    private final transient Region region;

    /**
     * Counter of talks.
     */
    private final transient Counter counter;

    /**
     * Public ctor.
     * @param reg Region
     * @param cnt Counter of talks
     */
    public DyTalks(final Region reg, final Counter cnt) {
        this.region = reg;
        this.counter = cnt;
    }

    @Override
    public boolean exists(final long number) {
        return this.region.table(DyTalks.TBL)
            .frame()
            .through(
                new QueryValve()
                    .withLimit(1)
                    .withIndexName(DyTalks.IDX_NUMBERS)
                    .withConsistentRead(false)
            )
            .where(DyTalks.ATTR_NUMBER, Conditions.equalTo(number))
            .iterator().hasNext();
    }

    @Override
    public Talk get(final long number) {
        return new DyTalk(
            this.region.table(DyTalks.TBL)
                .frame()
                .through(
                    new QueryValve()
                        .withLimit(1)
                        .withIndexName(DyTalks.IDX_NUMBERS)
                        .withConsistentRead(false)
                        .withSelect(Select.SPECIFIC_ATTRIBUTES)
                        .withAttributesToGet(DyTalks.HASH, DyTalks.ATTR_NUMBER)
                )
                .where(DyTalks.ATTR_NUMBER, Conditions.equalTo(number))
                .iterator().next()
        );
    }

    @Override
    public boolean exists(final String name) {
        return this.region.table(DyTalks.TBL)
            .frame()
            .through(new QueryValve().withLimit(1))
            .where(DyTalks.HASH, name)
            .iterator().hasNext();
    }

    @Override
    public Talk get(final String name) {
        return new DyTalk(
            this.region.table(DyTalks.TBL)
                .frame()
                .through(
                    new QueryValve()
                        .withLimit(1)
                        .withAttributesToGet(DyTalks.ATTR_NUMBER)
                )
                .where(DyTalks.HASH, name)
                .iterator().next()
        );
    }

    @Override
    public void delete(final String name) {
        Iterables.removeIf(
            this.region.table(DyTalks.TBL)
                .frame()
                .through(new QueryValve().withLimit(1))
                .where(DyTalks.HASH, name),
            item -> true
        );
    }

    @Override
    public void create(final String repo, final String name)
        throws IOException {
        final long number = this.counter.incrementAndGet(1L);
        this.region.table(DyTalks.TBL).put(
            new Attributes()
                .with(DyTalks.HASH, name)
                .with(DyTalks.ATTR_ACTIVE, Boolean.toString(true))
                .with(DyTalks.ATTR_REPO, repo)
                .with(DyTalks.ATTR_NUMBER, number)
                .with(DyTalks.ATTR_UPDATED, System.currentTimeMillis())
                .with(
                    DyTalks.ATTR_XML,
                    String.format("<talk name='%s' number='%d'/>", name, number)
                )
        );
    }

    @Override
    public Iterable<Talk> active() {
        return new Mapped<>(
            DyTalk::new,
            new HeadOf<>(
                10,
                this.region.table(DyTalks.TBL)
                    .frame()
                    .through(
                        new QueryValve()
                            .withIndexName(DyTalks.IDX_ACTIVE)
                            .withConsistentRead(false)
                            .withSelect(Select.SPECIFIC_ATTRIBUTES)
                            .withAttributesToGet(
                                DyTalks.HASH, DyTalks.ATTR_NUMBER
                            )
                    )
                    .where(DyTalks.ATTR_ACTIVE, Boolean.toString(true))
            )
        );
    }

    @Override
    public Iterable<Talk> recent() {
        return new HeadOf<>(
            5,
            new Filtered<>(
                input -> {
                    try {
                        return !input.read().nodes(
                            "/talk[@public='true']"
                        ).isEmpty();
                    } catch (final IOException ex) {
                        throw new IllegalStateException(ex);
                    }
                },
                new Mapped<>(
                    DyTalk::new,
                    this.region.table(DyTalks.TBL)
                        .frame()
                        .through(
                            new QueryValve()
                                .withIndexName(DyTalks.IDX_ACTIVE)
                                .withScanIndexForward(false)
                                .withConsistentRead(false)
                                .withLimit(5)
                                .withSelect(Select.ALL_PROJECTED_ATTRIBUTES)
                        )
                        .where(
                            DyTalks.ATTR_ACTIVE, Boolean.toString(false)
                        )
                )
            )
        );
    }

    @Override
    public Iterable<Talk> siblings(final String repo, final Date since) {
        return new Mapped<>(
            DyTalk::new,
            this.region.table(DyTalks.TBL)
                .frame()
                .through(
                    new QueryValve()
                        .withIndexName(DyTalks.IDX_SIBLINGS)
                        .withScanIndexForward(false)
                        .withConsistentRead(false)
                        .withLimit(20)
                        .withSelect(Select.ALL_PROJECTED_ATTRIBUTES)
                )
                .where(DyTalks.ATTR_REPO, repo)
                .where(
                    DyTalks.ATTR_UPDATED,
                    Condition.builder()
                        .comparisonOperator(ComparisonOperator.LT)
                        .attributeValueList(
                            AttributeValue.builder()
                                .n(Long.toString(since.getTime()))
                                .build()
                        )
                        .build()
                )
        );
    }
}
