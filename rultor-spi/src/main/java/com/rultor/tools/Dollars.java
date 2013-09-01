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
package com.rultor.tools;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

/**
 * Dollar amount.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "amount")
public final class Dollars {

    /**
     * Pattern to parse.
     */
    private static final Pattern PTN = Pattern.compile(
        "\\(?\\$([0-9]+(?:\\.[0-9]+)?)\\)?"
    );

    /**
     * Amount of it in millionth of dollar.
     */
    private final transient long amount;

    /**
     * Public ctor.
     * @param points Amount
     */
    public Dollars(final long points) {
        this.amount = points;
    }

    /**
     * Parse string.
     * @param text Text to parse
     * @return Dollars
     */
    public static Dollars valueOf(final String text) {
        final Matcher matcher = Dollars.PTN.matcher(text);
        Validate.isTrue(matcher.matches(), "invalid input '%s'", text);
        long points = new BigDecimal(matcher.group(1))
            .movePointRight(Tv.SIX).longValue();
        if (text.charAt(0) == '(') {
            points = -points;
        }
        return new Dollars(points);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final double usd = Math.abs((double) this.amount / Tv.MILLION);
        String body;
        if (usd == 0) {
            body = "$0.00";
        // @checkstyle MagicNumber (8 lines)
        } else if (usd < 0.00001d) {
            body = String.format("$%.6f", usd);
        } else if (usd < 0.0001d) {
            body = String.format("$%.5f", usd);
        } else if (usd < 0.001d) {
            body = String.format("$%.4f", usd);
        } else if (usd < 0.01d) {
            body = String.format("$%.3f", usd);
        } else {
            body = String.format("$%.2f", usd);
        }
        if (this.amount < 0) {
            body = String.format("(%s)", body);
        }
        return body;
    }

    /**
     * Points.
     * @return Points
     */
    public long points() {
        return this.amount;
    }

}
