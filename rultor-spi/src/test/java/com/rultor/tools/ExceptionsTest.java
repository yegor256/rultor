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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Exceptions}.
 * @author Krzysztof Krason (Krzysztof.Krason@gmail.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
public final class ExceptionsTest {
    /**
     * Message of the exception.
     */
    private static final String MESSAGE = "Message";

    /**
     * Message of the cause.
     */
    private static final String CAUSE = "Cause";

    /**
     * Format of exception messages for Throwable.
     */
    private static final String MSG_FORMAT = "Throwable: %s";

    /**
     * Handling of null exception.
     */
    @Test
    public void nullExceptionMessage() {
        MatcherAssert.assertThat(
            Exceptions.message(null), Matchers.equalTo(StringUtils.EMPTY)
        );
    }

    /**
     * Single exception with no cause.
     */
    @Test
    public void singleExceptionMessage() {
        final Throwable single = new Throwable(ExceptionsTest.MESSAGE);
        MatcherAssert.assertThat(
            Exceptions.message(single),
            Matchers.equalTo(
                String.format(
                    ExceptionsTest.MSG_FORMAT,
                    ExceptionsTest.MESSAGE
                )
            )
        );
    }

    /**
     * Exception with a cause.
     */
    @Test
    public void twoExceptionMessage() {
        final Throwable cause = new Throwable(ExceptionsTest.CAUSE);
        final Throwable first = new Throwable(ExceptionsTest.MESSAGE, cause);
        MatcherAssert.assertThat(
            Exceptions.message(first),
            Matchers.equalTo(
                StringUtils.join(
                    new String[] {
                        String.format(
                            ExceptionsTest.MSG_FORMAT,
                            ExceptionsTest.MESSAGE
                        ),
                        String.format(
                            ExceptionsTest.MSG_FORMAT,
                            ExceptionsTest.CAUSE
                        ),
                    },
                    SystemUtils.LINE_SEPARATOR
                )
            )
        );
    }
}
