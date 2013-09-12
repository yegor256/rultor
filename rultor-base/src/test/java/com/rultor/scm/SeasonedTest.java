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
package com.rultor.scm;

import com.google.common.collect.Iterators;
import com.jcabi.aspects.Tv;
import com.rultor.tools.Time;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Seasoned}.
 * @author Bharath Bolisetty (bharathbolisetty@gmail.com)
 * @version $Id$
 */
public final class SeasonedTest {

    /**
     * Seasoned can throw exception if public ctor args are null.
     * @throws Exception If some problem inside
     */
    @Test(expected = ConstraintViolationException.class)
    public void canThrowExceptionIfArgsAreNull() throws Exception {
        new Seasoned(1, null);
    }

    /**
     * Seasoned Can show only commits on or after given time.
     * @throws Exception If some problem inside
     */
    @Test
    public void canShowCommitsBeforeGivenTime() throws Exception {
        final Branch branch = Mockito.mock(Branch.class);
        final Commit before = Mockito.mock(Commit.class);
        final Commit after = Mockito.mock(Commit.class);
        Mockito.doReturn(
            Arrays.asList(
                before,
                after
            )
        ).when(branch).log();
        final long time = System.currentTimeMillis();
        Mockito.doReturn(
            new Time(time - TimeUnit.MINUTES.toMillis(Tv.THREE))
        ).when(before).time();
        Mockito.doReturn(
            new Time(time - TimeUnit.MINUTES.toMillis(1))
        ).when(after).time();
        final Branch seasoned = new Seasoned(2, branch);
        final Iterable<Commit> commitsitr = seasoned.log();
        MatcherAssert.assertThat(
            commitsitr,
            Matchers.hasItems(before)
        );
        MatcherAssert.assertThat(
            Iterators.size(
                commitsitr.iterator()
            ),
            Matchers.equalTo(1)
        );
    }
}
