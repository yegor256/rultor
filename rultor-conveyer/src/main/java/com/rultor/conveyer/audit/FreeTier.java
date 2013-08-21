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
package com.rultor.conveyer.audit;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.rultor.spi.Account;
import com.rultor.spi.Sheet;
import com.rultor.tools.Dollars;
import com.rultor.tools.Time;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Free tier.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
final class FreeTier {

    /**
     * Minimum threshold in points.
     */
    private static final long THRESHOLD = -Tv.FIVE * Tv.MILLION;

    /**
     * Period of silence in order to qualified for free tier, in days.
     */
    private static final int PERIOD = 30;

    /**
     * Add funds if necessary.
     * @param account Account to fund
     */
    public void fund(final Account account) {
        final long balance = account.balance().points();
        if (balance < FreeTier.THRESHOLD && !this.funded(account.sheet())) {
            account.fund(
                new Dollars(-FreeTier.THRESHOLD),
                String.format(
                    "Balance is lower than %s and no funds were added in the last %d days",
                    new Dollars(FreeTier.THRESHOLD),
                    FreeTier.PERIOD
                )
            );
        }
    }

    /**
     * Was it already funded recently?
     * @param sheet Sheet to check
     * @return TRUE if it was already funded
     */
    private boolean funded(final Sheet sheet) {
        final Time start = new Time(
            new Date().getTime() - TimeUnit.DAYS.toMillis(FreeTier.PERIOD)
        );
        return sheet
            .between(start, new Time())
            .where().equalTo("ct", Account.BANK.toString()).sheet()
            .iterator()
            .hasNext();
    }

}
