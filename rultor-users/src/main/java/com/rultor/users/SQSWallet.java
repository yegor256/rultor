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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rultor.aws.SQSClient;
import com.rultor.spi.Wallet;
import com.rultor.spi.Work;
import com.rultor.tools.Dollars;
import java.io.StringWriter;
import javax.json.Json;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * Wallet into Amazon SQS.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
final class SQSWallet implements Wallet {

    /**
     * SQS client.
     */
    private final transient SQSClient client;

    /**
     * Work we're in.
     */
    private final transient Work work;

    /**
     * Creditor.
     */
    private final transient URN creditor;

    /**
     * Credit unit.
     */
    private final transient String ctunit;

    /**
     * Debitor.
     */
    private final transient URN debitor;

    /**
     * Debit unit.
     */
    private final transient String dtunit;

    /**
     * Ctor.
     * @param sqs SQS client
     * @param wrk Work that is using us
     * @param ctr Creditor
     * @param cunit Credit unit
     * @param dtr Debitor
     * @param dunit Debit unit
     * @checkstyle ParameterNumber (5 lines)
     */
    protected SQSWallet(final SQSClient sqs, final Work wrk,
        final URN ctr, final String cunit, final URN dtr, final String dunit) {
        this.client = sqs;
        this.work = wrk;
        Validate.isTrue(
            !(ctr.equals(dtr) && cunit.equals(dunit)),
            "credit '%s/%s' can't be the same as debit '%s/%s'",
            ctr, cunit, dtr, dunit
        );
        this.creditor = ctr;
        this.ctunit = cunit;
        this.debitor = dtr;
        this.dtunit = dunit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void charge(final String details, final Dollars amount) {
        if (!this.creditor.equals(this.debitor)) {
            final AmazonSQS aws = this.client.get();
            try {
                aws.sendMessage(
                    new SendMessageRequest()
                        .withQueueUrl(this.client.url())
                        .withMessageBody(this.json(details, amount))
                );
            } finally {
                aws.shutdown();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Wallet delegate(final URN urn, final String unit) {
        final Wallet delegate = new SQSWallet(
            this.client, this.work, this.debitor, this.dtunit, urn, unit
        );
        return new Wallet() {
            @Override
            public void charge(final String details, final Dollars amount) {
                delegate.charge(details, amount);
                SQSWallet.this.charge(details, amount);
            }
            @Override
            public Wallet delegate(final URN urn, final String unit) {
                return delegate.delegate(urn, unit);
            }
        };
    }

    /**
     * Make JSON.
     * @param details Payment details
     * @param amount Dollar amount
     * @return JSON
     */
    private String json(final String details, final Dollars amount) {
        final StringWriter writer = new StringWriter();
        Json.createGenerator(writer)
            .writeStartObject()
            .write("ct", this.creditor.toString())
            .write("ctunit", this.ctunit)
            .write("dt", this.debitor.toString())
            .write("dtunit", this.dtunit)
            .write("details", details)
            .write("amount", amount.points())
            .writeStartObject("work")
            .write("owner", this.work.owner().toString())
            .write("unit", this.work.unit())
            .write("scheduled", this.work.scheduled().toString())
            .writeEnd()
            .writeEnd()
            .close();
        return writer.toString();
    }

}
