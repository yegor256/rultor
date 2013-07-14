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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.Conditions;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.urn.URN;
import com.rultor.spi.Dollars;
import com.rultor.spi.Invoice;
import com.rultor.spi.Invoices;
import com.rultor.spi.Time;
import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Invoices in DynamoDB.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "region", "name" })
@Loggable(Loggable.DEBUG)
final class AwsInvoices implements Invoices {

    /**
     * Dynamo DB table name.
     */
    public static final String TABLE = "invoices";

    /**
     * Dynamo DB table column.
     */
    public static final String KEY_OWNER = "owner";

    /**
     * Dynamo DB table column.
     */
    public static final String KEY_CODE = "code";

    /**
     * Dynamo DB table column.
     */
    public static final String FIELD_TEXT = "text";

    /**
     * Dynamo DB table column.
     */
    public static final String FIELD_AMOUNT = "amount";

    /**
     * Dynamo.
     */
    private final transient Region region;

    /**
     * URN of the user.
     */
    private final transient URN name;

    /**
     * Date to start with.
     */
    private final transient Time head;

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     * @param urn URN of the user
     */
    protected AwsInvoices(final Region reg, final URN urn) {
        this(reg, urn, new Time());
    }

    /**
     * Public ctor.
     * @param reg Region in Dynamo
     * @param urn URN of the user
     * @param time Date to start with
     */
    protected AwsInvoices(final Region reg, final URN urn, final Time time) {
        this.region = reg;
        this.name = urn;
        this.head = time;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Invoices tail(final Time time) {
        return new AwsInvoices(this.region, this.name, time);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Invoice> iterator() {
        assert this.head != null;
        final Iterator<Item> items = this.region.table(AwsInvoices.TABLE)
            .frame()
            .where(AwsInvoices.KEY_OWNER, Conditions.equalTo(this.name))
            .iterator();
        // @checkstyle AnonInnerLength (50 lines)
        return new Iterator<Invoice>() {
            @Override
            public boolean hasNext() {
                return items.hasNext();
            }
            @Override
            public Invoice next() {
                final Item item = items.next();
                return new Invoice() {
                    @Override
                    public Time date() {
                        return Invoice.Code.dateOf(
                            item.get(AwsInvoices.KEY_CODE).getS()
                        );
                    }
                    @Override
                    public Dollars amount() {
                        return new Dollars(
                            Long.parseLong(
                                item.get(AwsInvoices.FIELD_AMOUNT).getN()
                            )
                        );
                    }
                    @Override
                    public String text() {
                        return item.get(AwsInvoices.FIELD_TEXT).getS();
                    }
                };
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
