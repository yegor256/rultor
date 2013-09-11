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
     * Seasoned public ctor args can not be null.
     * @throws Exception If some problem inside
     */
    @Test(expected = ConstraintViolationException.class)
    public void argsCanNotBeNull() throws Exception {
        new Seasoned(1, null);
    }

    /**
     * Can show only commits on or after given time.
     * @throws Exception If some problem inside
     */
    @Test
    public void showsBeforeCommitsOnly() throws Exception {
        final Branch origin = Mockito.mock(Branch.class);
        final Commit beforecommit = Mockito.mock(Commit.class);
        final Commit aftercommit = Mockito.mock(Commit.class);
        Mockito.doReturn(
            Arrays.asList(
                beforecommit,
                aftercommit
            )
        ).when(origin).log();
        final long beforetime = TimeUnit.MINUTES.toMillis(3);
        final long aftertime = TimeUnit.MINUTES.toMillis(1);
        final long currenttime = System.currentTimeMillis();
        Mockito.doReturn(
            new Time(currenttime - beforetime)
        ).when(beforecommit).time();
        Mockito.doReturn(
            new Time(currenttime - aftertime)
        ).when(aftercommit).time();
        final Branch seasoned = new Seasoned(2, origin);
        final Iterable<Commit> commitsitr = seasoned.log();
        MatcherAssert.assertThat(
            commitsitr,
            Matchers.hasItems(beforecommit)
        );
        MatcherAssert.assertThat(
            Iterators.size(
                commitsitr.iterator()
            ),
            Matchers.equalTo(1)
        );
    }
}
