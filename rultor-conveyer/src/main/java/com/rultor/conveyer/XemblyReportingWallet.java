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
package com.rultor.conveyer;

import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.rultor.snapshot.XemblyLine;
import com.rultor.spi.Wallet;
import com.rultor.tools.Dollars;
import org.xembly.Directives;

/**
 * Wallet logging every charge in Xembly.
 *
 * @author Evgeniy Nyavro (e.nyavro@gmail.com)
 * @version $Id$
 * @since 1.0
 */
@Loggable(Loggable.DEBUG)
final class XemblyReportingWallet implements Wallet {

    /**
     * Underlying wallet.
     */
    private final transient Wallet wallet;

    /**
     * Directives to accumulate total charge.
     */
    private final transient Directives directives;

    /**
     * Public ctor.
     * @param underlying Underlying wallet to wrap
     */
    protected XemblyReportingWallet(Wallet underlying) {
        this.wallet = underlying;
        this.directives = new Directives()
            .xpath("/snapshot")
            .add("cost").set("0");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void charge(final String details, final Dollars amount) {
        new XemblyLine(
            this.directives.xset(String.format(". + %d", amount.points()))
        ).log();
        this.wallet.charge(details, amount);
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public Wallet delegate(final URN urn, final String name)
        throws NotEnoughFundsException {
        return this.wallet.delegate(urn, name);
    }

}
