/**
 * Copyright (c) 2009-2014, rultor.com
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
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.jcabi.urn.URN;
import com.rultor.tools.Dollars;
import javax.validation.constraints.NotNull;

/**
 * User account.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Account {

    /**
     * Who is sending funds when account is funded.
     */
    URN BANK = URN.create("urn:rultor:1");

    /**
     * Balance.
     * @return Balance
     */
    @NotNull(message = "Balance of account is never NULL")
    Dollars balance();

    /**
     * Sheet.
     * @return Sheet
     */
    @NotNull(message = "sheet is never NULL")
    Sheet sheet();

    /**
     * Fund account.
     * @param amount Amount to add/deduct
     * @param details Explanation
     */
    @NotNull(message = "account is never NULL")
    void fund(Dollars amount, String details);

    /**
     * Fund account with coupon.
     * @param code Coupon code to use for funding.
     * @throws InvalidCouponException When wrong coupon code is used.
     * @checkstyle RedundantThrowsCheck (5 lines)
     */
    void fund(String code) throws InvalidCouponException;
}
