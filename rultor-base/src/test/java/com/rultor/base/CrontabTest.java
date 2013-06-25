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
package com.rultor.base;

import com.rultor.spi.Instance;
import java.util.Calendar;
import java.util.TimeZone;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Crontab}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class CrontabTest {

    /**
     * Today.
     */
    private final transient Calendar today =
        Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    /**
     * Crontab can parse input text.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void parsesValidText() throws Exception {
        final String[] texts = new String[] {
            "* * * 3-7 *",
            "4 2,4,5 */4 * *",
            "* 4 * * *",
            "* * 4 */2 *",
            "* 2,3,4,5-8 * 4 *",
            "* * * * 4",
            "@daily",
            "@monthly",
            "@annually",
            "59 23 31 12 6",
        };
        for (String text : texts) {
            final Instance origin = Mockito.mock(Instance.class);
            final Crontab crontab = new Crontab(text, origin);
            crontab.pulse();
        }
    }

    /**
     * Crontab can pass through only at certain time moment and only once.
     * @throws Exception If some problem inside
     */
    @Test
    public void passesThroughOnlyWhenAllowed() throws Exception {
        final String text = String.format(
            "* %d %d %d *",
            this.today.get(Calendar.HOUR_OF_DAY),
            this.today.get(Calendar.DAY_OF_MONTH) + 1,
            this.today.get(Calendar.MONTH) + 1
        );
        final Instance origin = Mockito.mock(Instance.class);
        final Crontab crontab = new Crontab(text, origin);
        crontab.pulse();
        Mockito.verify(origin, Mockito.times(1)).pulse();
    }

    /**
     * Crontab can block when not allowed.
     * @throws Exception If some problem inside
     */
    @Test
    public void blocksWhenNotAllowed() throws Exception {
        final String text = String.format(
            "* %d %d %d   *   ",
            this.today.get(Calendar.HOUR_OF_DAY) + 1,
            this.today.get(Calendar.DAY_OF_MONTH) + 1,
            this.today.get(Calendar.MONTH) + 1
        );
        final Instance origin = Mockito.mock(Instance.class);
        final Crontab crontab = new Crontab(text, origin);
        crontab.pulse();
        Mockito.verify(origin, Mockito.times(0)).pulse();
    }

    /**
     * Crontab can calculate lag correctly.
     * @throws Exception If some problem inside
     */
    @Test
    public void calculatesLagCorrectly() throws Exception {
        final String text = String.format(
            "* %d * * *",
            this.today.get(Calendar.HOUR_OF_DAY) + 1
        );
        final Instance origin = Mockito.mock(Instance.class);
        final Crontab crontab = new Crontab(text, origin);
        MatcherAssert.assertThat(
            crontab.toString(),
            Matchers.endsWith(" in 1hr")
        );
    }

}
