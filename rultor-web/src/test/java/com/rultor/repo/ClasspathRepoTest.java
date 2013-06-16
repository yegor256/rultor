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
package com.rultor.repo;

import com.rultor.spi.Instance;
import com.rultor.spi.Pulseable;
import com.rultor.spi.Repo;
import com.rultor.spi.Spec;
import com.rultor.spi.State;
import com.rultor.spi.Work;
import java.util.concurrent.atomic.AtomicLong;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ClasspathRepo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class ClasspathRepoTest {

    /**
     * ClasspathRepo can make a spec.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesSpecFromText() throws Exception {
        final Repo repo = new ClasspathRepo();
        final String[] texts = new String[] {
            "com.rultor.repo.ClasspathRepoTest$Foo(55)",
            "java.lang.String( 'te   \n st' )",
            "java.lang.Integer ( 123 )",
            "com.rultor.repo.ClasspathRepoTest$Foo (55\n)",
            "com.first(com.second(com.third(), com.forth()))",
        };
        for (String text : texts) {
            MatcherAssert.assertThat(
                repo.make(text).asText(),
                Matchers.equalTo(repo.make(text).asText())
            );
        }
    }

    /**
     * ClasspathRepo can make an instance.
     * @throws Exception If some problem inside
     */
    @Test
    public void makesInstanceFromSpec() throws Exception {
        final Repo repo = new ClasspathRepo();
        final Spec spec = repo.make(
            "com.rultor.repo.ClasspathRepoTest$Foo(2)"
        );
        ClasspathRepoTest.Foo.COUNTER.set(0);
        final Instance instance = repo.make(spec);
        MatcherAssert.assertThat(
            ClasspathRepoTest.Foo.COUNTER.get(),
            Matchers.equalTo(2L)
        );
        final Work work = Mockito.mock(Work.class);
        instance.pulse(work, new State.Memory());
        MatcherAssert.assertThat(
            ClasspathRepoTest.Foo.COUNTER.get(),
            Matchers.equalTo(-1L)
        );
    }

    /**
     * Test class.
     */
    public static final class Foo implements Pulseable {
        /**
         * Static counter.
         */
        public static final AtomicLong COUNTER = new AtomicLong();
        /**
         * Public ctor.
         * @param number The number
         */
        public Foo(final long number) {
            ClasspathRepoTest.Foo.COUNTER.addAndGet(number);
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public void pulse(final Work work, final State state) {
            ClasspathRepoTest.Foo.COUNTER.set(-1);
        }
    }

}
