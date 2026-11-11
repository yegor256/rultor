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
import com.jcabi.dynamo.Frame;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.QueryValve;
import com.jcabi.dynamo.Region;
import com.rultor.spi.Talk;
import com.rultor.spi.Talks;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.cactoos.iterable.Filtered;
import org.cactoos.iterable.HeadOf;
import org.cactoos.iterable.Joined;
import org.cactoos.iterable.Mapped;
import org.cactoos.list.ListOf;
import org.cactoos.set.SetOf;
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
public final class DyTalks implements Talks {

    /**
     * Table name.
     */
    public static final String TBL = "talks";

    /**
     * Index name.
     */
    public static final String IDX_ACTIVE = "active";

    /**
     * Index name.
     *
     * @since 1.3
     */
    public static final String IDX_NUMBERS = "numbers";

    /**
     * Index name.
     *
     * @since 1.23
     */
    public static final String IDX_SIBLINGS = "siblings";

    /**
     * Talk unique name.
     */
    public static final String HASH = "name";

    /**
     * Its number.
     *
     * @since 1.3
     */
    public static final String ATTR_NUMBER = "number";

    /**
     * Name of the repo it belongs to.
     *
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
     * Is it activated and not yet modified since then (true) or not (false).
     *
     * @since 2.0
     */
    public static final String ATTR_FRESH = "fresh";

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
     *
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
            .frame().through(
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
                .frame().through(
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
                .frame().through(
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
                .with(DyTalks.ATTR_FRESH, Boolean.toString(false))
                .with(DyTalks.ATTR_REPO, repo)
                .with(DyTalks.ATTR_NUMBER, number)
                .with(DyTalks.ATTR_UPDATED, System.currentTimeMillis()).with(
                    DyTalks.ATTR_XML,
                    String.format("<talk name='%s' number='%d'/>", name, number)
                )
        );
    }

    @Override
    public Iterable<Talk> active() {
        final List<Item> fresh = new ListOf<>(
            new Filtered<>(
                item -> item.has(DyTalks.ATTR_FRESH)
                    && Boolean.parseBoolean(item.get(DyTalks.ATTR_FRESH).s()),
                new HeadOf<>(20, this.queue(false, 20))
            )
        );
        final Set<String> names = new SetOf<>(
            new Mapped<>(item -> item.get(DyTalks.HASH).s(), fresh)
        );
        return new Mapped<>(
            DyTalk::new,
            new HeadOf<>(
                10,
                new Joined<Item>(
                    fresh,
                    new Filtered<>(
                        item -> !names.contains(item.get(DyTalks.HASH).s()),
                        this.queue(true, 10)
                    )
                )
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
                        .frame().through(
                            new QueryValve()
                                .withIndexName(DyTalks.IDX_ACTIVE)
                                .withScanIndexForward(false)
                                .withConsistentRead(false)
                                .withLimit(5)
                                .withSelect(Select.ALL_PROJECTED_ATTRIBUTES)
                        ).where(
                            DyTalks.ATTR_ACTIVE, Boolean.toString(false)
                        )
                )
            )
        );
    }

    @Override
    public Iterable<Talk> siblings(final String repo, final Instant since) {
        return new Mapped<>(
            DyTalk::new,
            this.region.table(DyTalks.TBL)
                .frame().through(
                    new QueryValve()
                        .withIndexName(DyTalks.IDX_SIBLINGS)
                        .withScanIndexForward(false)
                        .withConsistentRead(false)
                        .withLimit(20)
                        .withSelect(Select.ALL_PROJECTED_ATTRIBUTES)
                )
                .where(DyTalks.ATTR_REPO, repo).where(
                    DyTalks.ATTR_UPDATED,
                    Condition.builder()
                        .comparisonOperator(ComparisonOperator.LT).attributeValueList(
                            AttributeValue.builder()
                                .n(Long.toString(since.toEpochMilli()))
                                .build()
                        )
                        .build()
                )
        );
    }

    private Frame queue(final boolean forward, final int limit) {
        return this.region.table(DyTalks.TBL)
            .frame().through(
                new QueryValve()
                    .withIndexName(DyTalks.IDX_ACTIVE)
                    .withScanIndexForward(forward)
                    .withLimit(limit)
                    .withConsistentRead(false)
                    .withSelect(Select.SPECIFIC_ATTRIBUTES).withAttributesToGet(
                        DyTalks.HASH, DyTalks.ATTR_NUMBER, DyTalks.ATTR_FRESH
                    )
            )
            .where(DyTalks.ATTR_ACTIVE, Boolean.toString(true));
    }
}
