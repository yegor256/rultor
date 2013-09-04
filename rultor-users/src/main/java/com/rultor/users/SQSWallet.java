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
import com.jcabi.log.Logger;
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
     * Credit rule.
     */
    private final transient String ctrule;

    /**
     * Debitor.
     */
    private final transient URN debitor;

    /**
     * Debit rule.
     */
    private final transient String dtrule;

    /**
     * Ctor.
     * @param sqs SQS client
     * @param wrk Work that is using us
     * @param ctr Creditor
     * @param crule Credit unit
     * @param dtr Debitor
     * @param drule Debit unit
     * @checkstyle ParameterNumber (5 lines)
     */
    protected SQSWallet(final SQSClient sqs, final Work wrk,
        final URN ctr, final String crule, final URN dtr, final String drule) {
        this.client = sqs;
        this.work = wrk;
        Validate.isTrue(
            !(ctr.equals(dtr) && crule.equals(drule)),
            "credit '%s/%s' can't be the same as debit '%s/%s'",
            ctr, crule, dtr, drule
        );
        this.creditor = ctr;
        this.ctrule = crule;
        this.debitor = dtr;
        this.dtrule = drule;
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
                Logger.info(
                    this,
                    "charged %s from %s to %s for \"%s\"",
                    amount, this.creditor, this.debitor, details
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
    public Wallet delegate(final URN urn, final String rule) {
        final Wallet delegate = new SQSWallet(
            this.client, this.work, this.debitor, this.dtrule, urn, rule
        );
        return new Wallet() {
            @Override
            public void charge(final String details, final Dollars amount) {
                delegate.charge(details, amount);
                SQSWallet.this.charge(details, amount);
            }
            @Override
            public Wallet delegate(final URN urn, final String rule)
                // @checkstyle RedundantThrows (1 line)
                throws Wallet.NotEnoughFundsException {
                return delegate.delegate(urn, rule);
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
            .write("ctrule", this.ctrule)
            .write("dt", this.debitor.toString())
            .write("dtrule", this.dtrule)
            .write("details", details)
            .write("amount", amount.points())
            .writeStartObject("work")
            .write("owner", this.work.owner().toString())
            .write("rule", this.work.rule())
            .write("scheduled", this.work.scheduled().toString())
            .writeEnd()
            .writeEnd()
            .close();
        return writer.toString();
    }

}
