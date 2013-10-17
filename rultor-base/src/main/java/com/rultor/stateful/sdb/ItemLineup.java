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
package com.rultor.stateful.sdb;

import com.google.common.collect.ImmutableMap;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.simpledb.Item;
import com.rultor.spi.Wallet;
import com.rultor.stateful.Lineup;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Lineup with synchronization through Amazon SimpleDB item.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "item", "wallet" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class ItemLineup implements Lineup {

    /**
     * Attribute name.
     */
    private static final String IDENTIFIER = "identifier";

    /**
     * Attribute name.
     */
    private static final String TIME = "time";

    /**
     * Randomizer.
     */
    private static final Random RAND = new SecureRandom();

    /**
     * Max waiting time in milliseconds.
     */
    private static final long MAX = TimeUnit.MINUTES.toMillis(Tv.FIVE);

    /**
     * Wallet to charge.
     */
    private final transient Wallet wallet;

    /**
     * SimpleDB item.
     */
    private final transient Item item;

    /**
     * Public ctor.
     * @param wlt Wallet to charge
     * @param itm Item
     */
    public ItemLineup(
        @NotNull(message = "wallet can't be NULL") final Wallet wlt,
        @NotNull(message = "item can't be NULL") final Item itm) {
        this.wallet = wlt;
        this.item = itm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public <T> T exec(final Callable<T> callable) throws Exception {
        final long start = System.currentTimeMillis();
        try {
            while (true) {
                final Marker marker = new Marker(callable);
                if (!this.exists()) {
                    this.save(marker);
                }
                final Marker saved = this.load();
                if (saved.equals(marker)) {
                    break;
                }
                if (saved.age() > ItemLineup.MAX) {
                    this.remove();
                    continue;
                }
                Logger.info(
                    this,
                    "SDB item %s is locked by %s for %[ms]s already...",
                    this.item, saved,
                    System.currentTimeMillis() - start
                );
                TimeUnit.MILLISECONDS.sleep(
                    ItemLineup.RAND.nextInt(Tv.THOUSAND)
                );
            }
            return callable.call();
        } finally {
            this.remove();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    public void exec(final Runnable runnable) {
        try {
            this.exec(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        runnable.run();
                        return null;
                    }
                    @Override
                    public String toString() {
                        return runnable.toString();
                    }
                }
            );
        // @checkstyle IllegalCatch (1 line)
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Item exists in SimpleDB.
     * @return TRUE if it exists
     */
    @RetryOnFailure(verbose = false)
    private boolean exists() {
        final long start = System.currentTimeMillis();
        final boolean exists = !this.item.isEmpty();
        this.wallet.charge(
            Logger.format(
                // @checkstyle LineLength (1 line)
                "checked existence of AWS SimpleDB item %s domain in %[ms]s: %B",
                this.item, System.currentTimeMillis() - start, exists
            ),
            new Dollars(Tv.FIVE)
        );
        return exists;
    }

    /**
     * Save text to SimpleDB object.
     * @param marker Content to save
     */
    @RetryOnFailure(verbose = false)
    private void save(final Marker marker) {
        final long start = System.currentTimeMillis();
        this.item.putAll(
            new ImmutableMap.Builder<String, String>()
                .put(ItemLineup.IDENTIFIER, marker.toString())
                .put(ItemLineup.TIME, new Time().toString())
                .build()
        );
        this.wallet.charge(
            Logger.format(
                "put AWS SimpleDB item %s in %[ms]s",
                this.item, System.currentTimeMillis() - start
            ),
            new Dollars(Tv.FIVE)
        );
    }

    /**
     * Load text from SimpleDB item (or empty if it doesn't exist).
     * @return The content loaded
     */
    @RetryOnFailure(verbose = false)
    private Marker load() {
        final long start = System.currentTimeMillis();
        String text = this.item.get(ItemLineup.IDENTIFIER);
        this.wallet.charge(
            Logger.format(
                "loaded AWS SimpleDB item %s in %[ms]s",
                this.item, System.currentTimeMillis() - start
            ),
            new Dollars(Tv.FIVE)
        );
        if (text == null) {
            text = "";
        }
        return new Marker(text);
    }

    /**
     * Remove object from SimpleDB.
     */
    @RetryOnFailure(verbose = false)
    private void remove() {
        final long start = System.currentTimeMillis();
        this.item.clear();
        this.wallet.charge(
            Logger.format(
                "removed AWS SimpleDB item %s in %[ms]s",
                this.item, System.currentTimeMillis() - start
            ),
            new Dollars(Tv.FIVE)
        );
    }

    /**
     * Marker used in items.
     */
    @Immutable
    @EqualsAndHashCode(of = "text")
    private static final class Marker {
        /**
         * Text of it.
         */
        private final transient String text;
        /**
         * Ctor.
         * @param txt Text of the marker
         */
        protected Marker(final String txt) {
            this.text = txt;
        }
        /**
         * Ctor.
         * @param callable Callable we're based on
         */
        protected Marker(final Callable<?> callable) {
            this(
                String.format(
                    "%s %d %s", new Time(), System.nanoTime(), callable
                )
            );
        }
        @Override
        public String toString() {
            return this.text;
        }
        /**
         * Get its age in milliseconds.
         * @return Milliseconds
         */
        public long age() {
            final Time time;
            if (this.text.isEmpty()) {
                time = new Time();
            } else {
                time = new Time(this.text.substring(0, this.text.indexOf(' ')));
            }
            return time.delta(new Time());
        }
    }

}
